import groovy.json.JsonSlurper
import groovyx.gpars.GParsPool
def url = "https://www.instagram.com"
def starttime = System.currentTimeMillis()


JsonSlurper slurper = new JsonSlurper()
jsonlist = []
proxylist = []
"https://raw.githubusercontent.com/fate0/proxylist/master/proxy.list"
    .toURL().text.eachLine() {proxies ->
        jsonlist.add(slurper.parseText(proxies))}
jsonlist.each {proxylist.add([it.host,it.port])}
println proxylist.size()

parsedjson = slurper.parseText("https://raw.githubusercontent.com/sunny9577/proxy-scraper/master/proxies.json".toURL().text)
def result = parsedjson.findAll { it.value instanceof List } 
          .values()                                         
          .flatten()
          .collect { [it.ip, it.port] }
result.each {proxylist.add(it)}
println proxylist.size()

"https://raw.githubusercontent.com/clarketm/proxy-list/master/proxy-list.txt"
    .toURL().text.splitEachLine(" ") {proxies ->
        proxylist.add(proxies[0].split(":")) }
println proxylist.size()

"https://raw.githubusercontent.com/TheSpeedX/SOCKS-List/master/socks.txt"
    .toURL().text.splitEachLine(" ") {proxies ->
        try{
            proxylist.add(proxies[0].split(":"))
            }
        catch(Exception e1) { println e1 }
        }
println proxylist.size()
proxylist = proxylist.unique()
println proxylist.size()
parsetime = System.currentTimeMillis() - starttime
println "parse time: " + (parsetime/1000)+" seconds"

goodproxies = []

GParsPool.withPool( proxylist.size() ) { proxylist.eachParallel
    { proxies ->
        try {
                port = proxies[1].toInteger()
                addr = proxies[0]
                SocketAddress proxyAddr = new InetSocketAddress(addr, port)
                Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddr)
                URL url1 = new URL("https://api.ipify.org")
                // def urls = ["https://icanhazip.com","https://ifconfig.co","https://ifconfig.me","https://api.ipify.org"]
                // URL testurl = new URL(urls[rnd.nextInt(urls.size)])

                URLConnection conn = url1.openConnection(proxy)
                conn.setConnectTimeout(3000)
                conn.setReadTimeout(3000)
                conn.followRedirects = false
                response = conn.getContent().text 
                // conn.connect()
                goodproxies.add(proxies)  
                // println "GOOD PROXY"
            }
        catch(Exception e) { 
            println e
          }
    }
    }


endtime = System.currentTimeMillis()
println ("found "+goodproxies.size()+ " good proxies in "+(endtime-starttime)/1000 +" s")

def json = groovy.json.JsonOutput.toJson(goodproxies)
new File("goodproxies.json").write(json)

time = new Date()
result = ("GROOVY: " + time.format("yyyy-MM-dd HH:mm:ss ") + "Tested: " + proxylist.size() + " found " + goodproxies.size() + " good proxies in " + (endtime-starttime)/1000 + " s")

File f = new File("results.txt")
def lines = f.readLines()
lines = lines.plus(result)
f.text = lines.join('\n')

