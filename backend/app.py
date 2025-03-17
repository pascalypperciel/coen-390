from flask import Flask
import psycopg2
import os
from dotenv import load_dotenv

load_dotenv()
app = Flask(__name__)

DB_HOST = os.getenv("DB_HOST")
DB_NAME = os.getenv("DB_NAME")
DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_PORT = os.getenv("DB_PORT")

def get_db_connection():
    return psycopg2.connect(
        user=DB_USER,
        password=DB_PASSWORD,
        host=DB_HOST,
        port=DB_PORT,
        database=DB_NAME
    )

from routes.db_testing_routes import *
from routes.batch_processing_routes import *

if __name__ == '__main__':
    app.run(host="127.0.0.1", port=5000, debug=True)
