# Project Contributions

## Team-49 

### Brian Kam (bdkam@uci.edu)
- **Task 1 - Autocomplete, Fuzzy Search, Task 3, Task 4, Recording**
### Seung Yup Yum (syyum@uci.edu)
- **Task 1 - FullText, Task 2, Task 3, Task 4, README.md**

### Project Demonstration Video
- **Watch Here**: [View the Project Demo](https://drive.google.com/file/d/1dmHOXeJXMZbJrCLydqOzbuoPp69IcbL8/view?usp=sharing)
- **URL**: [(https://drive.google.com/file/d/1dmHOXeJXMZbJrCLydqOzbuoPp69IcbL8/view?usp=sharing)]

## Connection Pooling
##### File Name/Path
###### All files that require SQL connection due to Prepared Statement
- [View the AddMovie](project1/src/AddMovie.java)
- [View the AddStar](project1/src/AddStar.java)
- [View the EmployeeLogin](project1/src/EmployeeLogin.java)
- [View the LoginServlet](project1/src/LoginServlet.java)
- [View the MainPage](project1/src/MainPage.java)
- [View the Metadata](project1/src/Metadata.java)
- [View the MovieList](project1/src/MovieList.java)
- [View the Payment](project1/src/Payment.java)
- [View the SingleMovie](project1/src/SingleMovie.java)
- [View the SingleStar](project1/src/SingleStar.java)
- [View the inconsistencyMovie](project1/inconsistencyMovie.txt)
###### Changed META-INF/context.xml url
- [View the META-INF/context.xml](project1/WebContent/META-INF/context.xml)

##### How Connection Pooling is utilized in the Fabflix code
- `factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"`
- `maxTotal="100" maxIdle="30" maxWaitMillis="10000"`
-  url="jdbc:mysql://MASTER_PRIVATE_IP/moviedb?autoReconnect=true&amp;allowPublicKeyRetrieval=true&amp;useSSL=false&amp;cachePrepStmts=true"/>
-  url="jdbc:mysql://SLAVE_PRIVATE_IP/moviedb?autoReconnect=true&amp;allowPublicKeyRetrieval=true&amp;useSSL=false&amp;cachePrepStmts=true"/>
-  By doing this, maximum number of active connection is 100, the maximum number of idle connections is 30, the maximum amount of time (in milliseconds) that the pool will wait for a connection to be returned before throwing an exception is 10000 milliseconds.

##### How Connection Pooling works with two backend SQL
- We seperate two sql for specific task. They are defined in context.xml, named `moviedb_write`, `moviedb_read`. By doing this, we sepearte pools for each database backend and each has their own pool.
- Through context.xml, the username and password of MySQL is given to JDBC, and JDBC creates pool by using creadential.

## Master/Slave
##### File Name/Path
###### Changed META-INF/context.xml url
- [View the META-INF/context.xml](project1/WebContent/META-INF/context.xml)
- [View the Master Instance mysql cnf file](instance2mysqld.cnf)
- [View the Slave Instance mysql cnf file](instance3mysqld.cnf)

##### How read/write requests were routed to Master/Slave SQL
- In context.xml, we defined two datasource for wrtie operation and read opertaion. Since the write operation (INSERT INTO, stored procedure, stored function etc) into slave instance cannot affect master slave, we must direct to master instance when the laod balancer assign to slave instance.
- So datasource for write opertaion, we use MASTER_PRIVATE_IP, and for read operation, we use SLAVE_PRIVATE_IP.
- Since currently, the number task that requires read operation is much more than the number of task that requires write operation, we use SLAVE_PRIVATE_IP instead of localhost.
- In order to achieve multi way connection instead of using localhost for read operation, we need to give all privilege for our defined user in both instances.
- The command for giving all privilege is : `CREATE USER 'mytestuser'@'%' IDENTIFIED BY 'My6$Password'; GRANT ALL PRIVILEGES ON * . * TO 'mytestuser'@'%'; flush privileges;`
