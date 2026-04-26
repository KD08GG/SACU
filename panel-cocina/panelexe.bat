@echo off
echo =====================================
echo  Setting up project with Python 3.11
echo =====================================

REM 1. Create virtual environment only if it does not exist
if not exist .venv (
    echo Virtual environment not found...
    echo Creating virtual environment with Python 3.11...
    py -3.11 -m venv .venv

    REM Activate virtual environment
    call .venv\Scripts\activate

    REM Upgrade pip tools
    echo Upgrading pip, setuptools, wheel...
    python -m pip install --upgrade pip setuptools wheel

    REM Install project requirements
    echo Installing requirements...
    pip install -r requirements.txt

    REM Install Firebase Admin SDK
    echo Installing firebase-admin...
    pip install firebase-admin
) else (
    echo Virtual environment already exists.
    call .venv\Scripts\activate
)

REM 2. Run project
echo Running main.py...
python main.py

pause