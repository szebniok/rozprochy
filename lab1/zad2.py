import socket

serverIP = "127.0.0.1"
serverPort = 12345
msg = "żółta gęś"
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
client.sendto(bytes(msg, 'windows-1250'), (serverIP, serverPort))
(reply, addr) = client.recvfrom(1024)
print(f"received {reply} from {addr}")