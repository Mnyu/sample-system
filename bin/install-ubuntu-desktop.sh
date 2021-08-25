sudo apt update

export DEBIAN_FRONTEND=noninteractive

sudo apt install -y xfce4
sudo apt install -y tightvncserver

sudo apt install -y openjdk-11-jdk
sudo apt install -y visualvm
sudo apt install -y firefox 

cd /usr/share && \
sudo curl -LO http://apachemirror.wuchna.com//jmeter/binaries/apache-jmeter-5.3.tgz && \
sudo tar -zxf apache-jmeter-*.tgz && \
sudo rm -f apache-jmeter-*.tgz && \
sudo mv apache-jmeter-* jmeter && \
sudo ln -s /usr/share/jmeter/bin/jmeter /usr/bin/jmeter

sudo apt install -y xfce4-terminal
sudo apt install -y autocutsel

sudo mkdir -p /usr/data/jmeter && \
sudo mkdir -p /usr/data/shared

printf "password\npassword\n\n" | vncpasswd
vncserver
vncserver -kill :1
echo "xfce4-session &" >> ~/.vnc/xstartup
echo "xfce4-panel &" >> ~/.vnc/xstartup
echo "x-window-manager &" >> ~/.vnc/xstartup
echo "xfce4-terminal &" >> ~/.vnc/xstartup
echo "autocutsel &" >> ~/.vnc/xstartup

