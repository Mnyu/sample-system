#!/bin/bash

cd ~
curl -o kubectl https://amazon-eks.s3.us-west-2.amazonaws.com/1.18.8/2020-09-18/bin/linux/amd64/kubectl
chmod +x kubectl
sudo mv kubectl /usr/local/bin
sudo ln -s /usr/local/bin/kubectl /usr/bin/kubectl
