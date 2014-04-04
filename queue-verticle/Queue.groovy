def eb = vertx.eventBus

def queueConf = [
        "address": "vert-x.process.queue",
        "persistor_address": 'vertx.mongopersistor',
        "collection": "process.queue"
]

container.with {
    deployModule('vertx.mongo-persistor-v1.2', ["db_name": "queue_list"], 1) {
        println "Mongo in place."

        deployModule('vertx.work-queue-v1.2', queueConf, 1) {
            println "Queue started..."

            // Delete existing data from Queue
            eb.send(
                queueConf['persistor_address'], 
                ['action': 'delete', 'collection': queueConf['collection'], 'matcher': [:]])
        }
    }

    startHttpServer()
}

def startHttpServer() {
    def server = vertx.createHttpServer()
    def staticAssets = "web"
    def port = 8080

    // Serve the static resources
    server.requestHandler { req ->
        if (req.uri == '/') {
            req.response.sendFile('index.html')
        } else if (req.uri == '*.jpg') {
            req.response.sendFile(req.uri)
        } else {
            req.response.sendFile(staticAssets + req.uri)
        }
    }

    vertx.createSockJSServer(server).bridge(prefix: '/eventbus', [[:]])

    server.listen port

    println "Server started at http://localhost:${port}/"
}
