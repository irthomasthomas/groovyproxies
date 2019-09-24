import random
from multiprocessing.dummy import Pool as ThreadPool 
from requests import get, Session
from datetime import datetime
import json

def check_proxy(proxy):
    session = Session()
    proxies = {
        'http': proxy,
        'https': proxy
    }
    url = 'https://instagram.com'  
    # if random.randint(1, 2) == 1: url = 'https://api.ipify.org'    
    # else: url = 'https://www.w3.org'
    
    try:    
        session.get(url,proxies=proxies,timeout=3, allow_redirects=False)
        return proxy
    except:
        return

def getproxies():
    stime = datetime.now()

    # LIST 1
    with open("proxy.list","w") as jf:
        jf.write(get("https://raw.githubusercontent.com/fate0/proxylist/master/proxy.list").text)
    jsonlist = []
    with open("proxy.list","r") as jf:
        for x in jf:
            jsonlist.append(json.loads(x))
    proxylist = []
    for p in jsonlist:
        ip = str(p['host'])+":"+str(p['port'])
        if ip not in proxylist:
            proxylist.append(ip)
    print("LIST 1: " + str(len(proxylist)))
    
    # LIST 2
    prsc = json.loads(get("https://raw.githubusercontent.com/sunny9577/proxy-scraper/master/proxies.json").text)
    sourcelist = []
    for i in prsc:
        sourcelist.append(i)
    sourcelist.remove("lastUpdated")
    for source in sourcelist:
        for i in prsc[source]:
            ip =str(i["ip"])+":"+str(i['port'])
            if ip not in proxylist:
                proxylist.append(ip)
    print("LIST 2: " + str(len(proxylist)))
    # LIST 3
    for line in get("https://raw.githubusercontent.com/clarketm/proxy-list/master/proxy-list.txt").content.splitlines(True):
        p = line.decode().rstrip().split(" ")[0].split(":")
        try:
            ip = str(p[0])+":"+str(p[1])
            if ip not in proxylist:
                proxylist.append(ip)
        except:
            continue
    print("LIST 3: " + str(len(proxylist)))
    # LIST 4
    for line in get("https://raw.githubusercontent.com/TheSpeedX/SOCKS-List/master/socks.txt").content.splitlines(True):
        try:
            p = line.decode().rstrip().split(" ")[0].split(":")
            ip = str(p[0])+":"+str(p[1])
            if ip not in proxylist:
                proxylist.append(ip)
        except:
            continue
    print("LIST 4: " + str(len(proxylist)))
    etime = datetime.now()
    return proxylist

start_time = datetime.now()

proxylist = getproxies()
goodproxies = []
pool = ThreadPool(len(proxylist))

goodproxies = [x for x in pool.map(check_proxy, proxylist) if x is not None]

with open("pythonproxies.json","w") as file:
        json.dump(proxylist, file)

lap2 = datetime.now() - start_time
result = ("PYTHON(REQUESTS): "+str(datetime.now())+" Tested " + str(len(proxylist)) + " Found: " + str(len(goodproxies)) + " good proxies in " + str(lap2))
print(result)

with open("results.txt","w") as file:
        file.write(result)