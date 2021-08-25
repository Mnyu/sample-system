#!/bin/bash

echo pg_isready -h postgres
if [ $? = 0 ]; then
    echo "Ready"
else
    echo "Not Ready" >&2
fi
