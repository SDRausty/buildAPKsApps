#!/usr/bin/python

import socket

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(("", 9999))
print "Listening..."
while True:
    data, addr = sock.recvfrom(1024)
    print "Received:", data

