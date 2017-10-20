#!/bin/bash

echo /opt/sds/database/mongodb/bin/mongo

function export_db( )
{
   /opt/sds/database/mongodb/bin/mongoexport -u sc -p sds -d securitycontroller -c espc_table -o espc_table.dat
}


function import_db( )
{
    printf "Will import data, please delete table [espc_table] in mongodb, use command: \n\t/opt/sds/database/mongodb/bin/mongo\n\tuse securitycontroller;\n\tdb.espc_table.drop();\n"
    echo "Have you deleted the table? (Y/N) "
    read user
    
    if [[ "$user" == "Y" || "$user" == "y" ]] ;then
        /opt/sds/database/mongodb/bin/mongoimport -u sc -p sds -d securitycontroller -c espc_table espc_table.dat
    else
        echo "Import failed."
    fi
}

case "$1" in
    export)
        export_db
        RETVAL=$?
        ;;
    import)
        import_db
        RETVAL=$?
        ;;
    *)
        echo $"Usage: $0 {export|import}"
        RETVAL=2
        ;;
esac
exit $RETVAL

