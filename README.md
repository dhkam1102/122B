# Project Contributions

## Team-49 

### Brian Kam (bdkam@uci.edu)
- **task 2, 5, README, Creating txt, Video Recording**
### Seung Yup Yum (syyum@uci.edu)
- **task 1, 3, 4, 5**

### Project Demonstration Video
- **Watch Here**: [View the Project Demo](https://drive.google.com/file/d/1lSbh_voW3_3mIEo0134hWimvCdj_lhQx/view?usp=sharing)
- **URL**: [(https://drive.google.com/file/d/1lSbh_voW3_3mIEo0134hWimvCdj_lhQx/view?usp=sharing)]

## Supstring match design

```java
if (year != null && !year.isEmpty()) {
                    mid_query.append("AND m.year = '").append(year).append("' ");
                }
                if (director != null && !director.isEmpty()) {
                    mid_query.append("AND m.director LIKE '%").append(director).append("%' ");
                }
                if (name != null && !name.isEmpty()) {
                    mid_query.append("AND s.name LIKE '%").append(name).append("%' ");
                }
                if (title != null && !title.isEmpty()) {
                    mid_query.append("AND m.title LIKE '%").append(title).append("%' ");
                }
MovieList.java line 291 - 302
Director Name = m.director LIKE '%NAME%'
Star Name = s.name LIKE '%NAME%'
Movie Title = m.title LIKE '%TITLE%'
```

## stored-procedure.sql.

- **stored-procedure.sql outside of the project1 Folder**
- [View the stored-procedure.sql](stored-procedure.sql)
  
## Prepared Statements.

- AddStar, EmployeeLogin, LoginServlet, MainPage, MetaData, MovieList, SingleMovie, SingleStar

### Parsing Time Optimization

1. **Using MySQL LOAD Statement:** Place parsing results into CSV files (`stars.csv`, `stars_in_movies.csv`, `movies.csv`, `genres_in_movies.csv`) and load them into the database by running `RunParsing.java`.

2. **Using SAX Parser:** Use SAX parser instead of DOM parser to reduce the time to build the whole tree for big XML documents.

3. **Creating Classes for Itemizing Tree Structure:** For each XML parsing, create classes for itemizing tree structure to reduce the time to retrieve items since it does not insert each time.

4. **Obtaining O(1) Retrieval:** Use `HashMap` appropriately for O(1) retrieval of parsed data from classes.

5. **Using Prepared Statements:** Use Prepared Statements for every query.

## Additional Reports and Files

- **inconsistencyMovie.txt in project1 Folder**
- [View the Inconsistency Movie Report](project1/inconsistencyMovie.txt)
- **inconsistencyStar.txt in project1 Folder**
- [View the Inconsistency Star Report](project1/inconsistencyStar.txt)
