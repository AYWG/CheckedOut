# Take in Crawler Data and sends it to server
# Take in User Request and sends back item
import socket
import json


class query:
    def __init__(self):
        self.sock = socket.socket()
        self.host = socket.gethostname()
        self.port = 6969
        self.data = {}

    def start_query(self):
        self.message = {}
        self.data['name'] = 'oranges'
        self.data['store'] = 'save on foods'
        self.data['price'] = '1.69'
        self.message['data'] = self.data
        json_formatted_data = json.dumps(self.message)
        print(json_formatted_data)

        self.sock.connect((self.host, self.port))
        self.sock.send(json_formatted_data.encode())
        # self.sock.close()

    def start_query2(self):
        self.data['name'] = 'oranges'
        self.data['store'] = 'superstore'
        self.data['price'] = '1.50'
        json_formatted_data_2 = json.dumps(self.data)
        print(json_formatted_data_2)

        self.sock.connect((self.host, self.port))
        self.sock.send(json_formatted_data_2.encode())
        self.sock.close()

if __name__ == "__main__":
    x = query()
    # y = query()
    x.start_query()
    # y.start_query2()



"""
def query():
    user_request = "Oranges"
    # print(user_request)
    return user_request


def spider():
    data_in = {
        "name": "Oranges",
        "store": "SuperStore",
        "price": 2.99
    }

    name = "Oranges"
    # print(name)
    # print(data_in)
    return name, data_in


"""

