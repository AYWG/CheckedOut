import socket
import json
from pymongo import MongoClient

from RequestHandler import RequestHandler


class DatabaseServer:
    def test_server(self):
        # set up server socket
        server_socket = socket.socket()
        host = socket.gethostbyname(socket.gethostname())
        port = 6969
        server_socket.bind((host, port))
        server_socket.listen(10)

        # connect to MongoDB
        client = MongoClient()
        categories_db = client.categories
        item_db = client.items

        request_handler = RequestHandler(categories_db, item_db)

        while True:
            connection, addr = server_socket.accept()
            data = connection.recv(1024).decode()
            json_data = json.loads(data)

            response = request_handler.handle_request(json_data['message_type'], json_data)
            # response = {'message_type': 'read_response', 'items': ['maple syrup']}
            print(response)
            json_response = json.dumps(response)
            connection.send(json_response.encode())
            connection.close()