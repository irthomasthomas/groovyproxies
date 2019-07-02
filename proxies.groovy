import groovyx.gpars.GParsPool
import groovy.time.TimeCategory 
import groovy.time.TimeDuration

proxylist = []
File proxyfile = new File("proxieslist.txt")
proxyfile.splitEachLine(" ") {proxies ->
    try{
        proxylist.add(proxies[0].split(":").collect())
    }
    catch(Exception e) {println e}
}
proxylist = proxylist.unique()
println "Downloaded "+proxylist.size() + " proxies"
println "Now testing..." 

goodproxies = []
Random rnd = new Random()
starttime = System.currentTimeMillis()

GParsPool.withPool(proxylist.size()) {proxylist.eachParallel { proxies ->
    try {
        port = proxies[1].toInteger()
        addr = proxies[0]
        SocketAddress proxyAddr = new InetSocketAddress(addr,port)
        Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyAddr)
        def urls = ["https://icanhazip.com","https://ifconfig.co","https://ifconfig.me","https://api.ipify.org"]
        // set a random URL so we aren't flooding a single host.
        URL testurl = new URL(urls[rnd.nextInt(urls.size)])

        URLConnection con = testurl.openConnection(proxy) // 1500ms
        con.setFollowRedirects(false)
        con.setConnectTimeout(1000) 
        con.setReadTimeout(5000)        
        con.connect()
        goodproxies.add(proxies)
    }
    catch(Exception e) {    }
    }
}

endtime = System.currentTimeMillis()
println ("found "+goodproxies.size()+ " good proxies in "+(endtime-starttime)/1000 +" s")

File lstFile = new File("goodproxies.txt")
lstFile.withWriter{ out ->
  goodproxies.each {out.println it }
  }

time = new Date()
result = (time.format("yyyy-MM-dd HH:mm:ss ") + " found " + goodproxies.size() + " good proxies in " + (endtime-starttime)/1000 + " s")

File f = new File("results.txt")
def lines = f.readLines()
lines = lines.plus(result)
f.text = lines.join('\n')

