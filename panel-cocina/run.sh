#!/bin/bash
echo "====================================="
echo " Setting up project with Python 3.11"
echo "====================================="

# 1. Create virtual environment only if it does not exist
if [ ! -d ".venv" ]; then
    echo "Virtual environment not found..."
    echo "Creating virtual environment with Python 3.11..."
    python3.11 -m venv .venv

    source .venv/bin/activate

    echo "Upgrading pip, setuptools, wheel..."
    python -m pip install --upgrade pip setuptools wheel

    echo "Installing requirements..."
    pip install -r requirements.txt

    echo "Installing firebase-admin..."
    pip install firebase-admin
else
    echo "Virtual environment already exists."
    source .venv/bin/activate
fi

# 2. Run project
echo "Running main.py..."
python main.py
