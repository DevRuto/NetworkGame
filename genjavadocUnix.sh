#!/bin/bash
cd Client
chmod +x gendocs.sh
./gendocs.sh
cd ../Server
chmod +x gendocs.sh
./gendocs.sh
