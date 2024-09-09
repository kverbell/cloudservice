package ru.netology.cloudservice.CustomSqliteDialect;

import org.hibernate.MappingException;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

public class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {

    @Override
    public boolean supportsIdentityColumns() {
        return true;
    }

    @Override
    public String getIdentitySelectString(String table, String column, int type) throws MappingException {
        return "select last_insert_rowid()";
    }

    @Override
    public String getIdentityColumnString(int type) throws MappingException {
        return "type = integer";
    }

    @Override
    public boolean supportsInsertSelectIdentity() {
        return false;
    }

    @Override
    public String appendIdentitySelectToInsert(String insertSQL) {
        throw new MappingException("SQLite does not support appending identity select to insert");
    }
}
