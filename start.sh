var_defined()
{
    local var_name=$1
    set | grep "^${var_name}=" 1>/dev/null
    return $?
}

if var_defined JAVA_HOME; then
    echo "JAVA_HOME is defined"
else
    export JAVA_HOME=`/usr/libexec/java_home -v 1.7`
fi

echo "Contents of JAVA_HOME " $JAVA_HOME

cd $HOME/tools/apache-tomcat-7.0.34/bin
./startup.sh


