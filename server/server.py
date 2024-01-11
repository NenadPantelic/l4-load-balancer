import socket
import select
import json
import datetime


def now():
    return datetime.datetime.now()


def generate_http_output(address_port, client_address, client_message):
    return bytes(f"""HTTP/1.1 200 OK\r\nContent-type: text/html\r\nSet-Cookie: ServerName=np-server\r
        \r\n
        <!doctype html>
        <html>
            <head/>
            <body>
                <h1>Welcome to the server!</h1>
                <h2>Server address: {address_port[0]}:{address_port[1]}</h2>
                <h3>You're connected through address: {client_address[0]}:{client_address[1]}</h3>
                <h4>Uptime in millis: {(now() - current_timestamp).seconds * 1000}</h3>
                <body>
                    <pre>{client_message.decode("utf-8")}<pre>
                </body>
            </body>
        </html>
        \r\n\r\n
        """, "utf-8")


if __name__ == '__main__':
    current_timestamp = now()
    # Get socket file descriptor as a TCP socket using the IPv4 address family
    listener_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # Set some modes on the socket, not required but it's nice for our uses
    listener_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    host = '0.0.0.0'
    port = 9000
    # if the server will be run in Docker container, use 0.0.0.0 interface here, so all NIC addresses are listened to;
    # Docker typically uses this interface 172.17.0.x
    address_port = (host, port)
    # serve address and port
    listener_socket.bind(address_port)
    # listen for connections, a maximum of 1
    listener_socket.listen(1)

    print(f'Server listening @ {host}:{port}')

    # loop indefinitely to continuously check for new connections
    while True:
        # Poll the socket to see if there are any newly written data, note excess data dumped to "_" variables
        ready_sockets, _, _ = select.select(
            # list of items we want to check for read-readiness (just our socket)
            [listener_socket],
            # list of items we want to check for write-readiness (not relevant for this case)
            [],
            # list of items we want to check for "exceptional" conditions (also not relevant for this case)
            [],
            0  # timeout of 0 seconds, makes the method call non-blocking
        )
        # if the value was returned here then there is a connection to read from
        if ready_sockets:
            # select.select() returns a list of readable objects, so we'll iterate, but we only expect a single item
            for ready_socket in ready_sockets:
                # accept the connection from the client and get its socket object and address
                client_socket, client_address = ready_socket.accept()

                # read up to 4096 bytes of data from the client socket
                client_message = client_socket.recv(4096)
                print(f'Client sent: {client_message.decode("utf-8")}')

                # we send the HTTP response, so HTTP client can actually accept it (render it in case browser sent the
                # request)
                client_socket.sendall(generate_http_output(
                    address_port, client_address, client_message))
                try:
                    # close the connection
                    client_socket.close()
                except OSError:
                    # client disconnected first, nothing to do
                    pass
