@echo off
echo =====================================
echo  Setting up project with Python 3.11
echo =====================================

REM 1. Remove old virtual environment
if exist .venv (
    echo Removing old virtual environment...
    rmdir /s /q .venv
)

REM 2. Create new virtual environment with Python 3.11
echo Creating virtual environment with Python 3.11...
py -3.11 -m venv .venv

REM 3. Activate virtual environment
call .venv\Scripts\activate

REM 4. Upgrade pip tools
echo Upgrading pip, setuptools, wheel...
python -m pip install --upgrade pip setuptools wheel

REM 5. Install project requirements
echo Installing requirements...
pip install -r requirements.txt

REM 6. Install Firebase Admin SDK
echo Installing firebase-admin...
pip install firebase-admin

REM 7. Run project
echo Running main.py...
python main.py

pause