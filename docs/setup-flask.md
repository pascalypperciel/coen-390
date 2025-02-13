# Setting Up the Flask Environment

Follow these steps to get started with the Flask backend for this project.

## 1. Install Python
Download and install **Python** by following the official instructions here:  
[**Download Python 3.13.2**](https://www.python.org/downloads/release/python-3132/)

 **Recommended version**: **3.13.2** (This is the version I am using.)

 **Note**: You can verify your installation by running the following command in your terminal. You may need to restart your computer for it to work:
 ```
 py --version
 ```

## 2. Set Up a Virtual Environment
Make sure you are in the **backend** directory in your terminal with the following command:
```
cd backend
```
Run the following commands in your terminal:
```
py -m venv venv
```
Then run this:
```
venv\Scripts\activate           // if your terminal is a Command Prompt
venv\Scripts\Activate.ps1       // if your terminal is a PowerShell
```

## 3. Install Dependencies
This is the command to install all the dependencies. You can re-run that command at any time, what it does is that it installs every packages in the **requirements.txt** file, and even makes sure that you have the same version.
```
pip install -r requirements.txt
```

## 4. Set Up the Database Connection String
So in order to access the database, you will require to [**download the .env file**](https://drive.google.com/file/d/1vuFKdbHLn6MdlfHscSPd-qZvx7gsAypB/view?usp=sharing) (You will have to request access first, for safety reasons) and then add the downloaded file into your **backend** directory.

## 5. Post-Install Notes
Here are some good-to-knows.

**How do I run my code?**:
```
py app.py
```

**If you install new Python packages**: Please run the following command to add that package into **requirements.txt** so that your teammates can easily install it later on their own virtual environment.
```
pip freeze > requirements.txt
```
