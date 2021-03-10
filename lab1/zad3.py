import socket

serverIP = "127.0.0.1"
serverPort = 12345
msg_bytes = (300).to_bytes(4, byteorder='little')
client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
client.sendto(msg_bytes, (serverIP, serverPort))
(buff, addr) = client.recvfrom(1024)
reply = int.from_bytes(buff, byteorder='little')
print(f"received {reply} from {addr}")