package com.cmon.mysql;

import org.hibernate.dialect.MySQL5InnoDBDialect;

public class UTF8MysqlDialect extends MySQL5InnoDBDialect {
	@Override
	public String getTableTypeString() {
		// Reference: http://blog.tremend.ro/2007/08/14/how-to-set-the-default-charset-to-utf-8-for-create-table-when-using-hibernate-with-java-persistence-annotations/
		return " ENGINE=InnoDB DEFAULT CHARSET=utf8";
	}
}
