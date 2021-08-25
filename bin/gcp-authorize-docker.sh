#!/bin/bash

echo "-- Authorize Docker for Google Docker Registry --"
gcloud auth login
gcloud auth configure-docker -q
