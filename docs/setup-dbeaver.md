# Connecting to the Database using DBeaver

Follow these steps to connect to our database using **DBeaver**. This will let you run queries to the database.

## 1. Download/Open DBeaver
Download and install **DBeaver** by following the official instructions here:  
[**Download DBeaver**](https://dbeaver.io/download/)

Launch the **DBeaver** application on your computer.

## 2. Create a New Connection
- Click on the **"New Database Connection"** button in the toolbar at the top left-side.
- Alternatively, click on the **"Database"** menu and select **"New Database Connection"**.

## 3. Select PostgreSQL
- In the **"Connect to a database"** dialog, choose **"PostgreSQL"** from the list of available databases.
- Click **"Next"**.

## 4. Configure the Connection
- **Host**:  
  Enter: `minicap-db-server.postgres.database.azure.com`
- **Port**:  
  Enter: `5432`
- **Database**:  
  Enter: `postgres`
- **Username**:  
  Enter: `admin_username`
- **Password**:  
  Come and ask me (Pascal) on Discord.

## 5. Configure Connection Settings
- Click on the **"Driver properties"** tab in the **"Connection settings"** window.
- If it prompts you to download drivers, download them.

## 6. Test the Connection
- Click the **"Test Connection"** button to ensure DBeaver can successfully connect to the **Azure PostgreSQL Flexible Server**.
- If the test is successful, you'll see a **"Connected"** message.
- If the test fails:
  - Double-check your connection settings.
  - Try again.
  - You may be prompted to **download the PostgreSQL driver**.

## 7. Finish and Connect
- Once the connection is successful, click **"Finish"**.
- DBeaver will now connect to the **Azure PostgreSQL Flexible Server**.
- You will see the connection in the **Database Navigator** panel on the left side of the DBeaver window.

## 8. Access the Database
- Once connected, expand the server in the browser tree to view:
  - **Databases**
  - **Schemas**
  - **Tables**
- You can also interact with the server using the built-in **query tool** and manage your database objects.