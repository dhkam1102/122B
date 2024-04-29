# Project Contributions

## Team-49 

### Brian Kam (bdkam@uci.edu)
- **login page**, **payment page**, **Video Recording**
- **task 1, 4**
### Seung Yup Yum (syyum@uci.edu)
- **Main page**, **Main page sorting**, **Video Recording**, **html visual appearnce**
- **task 2,3**

### Project Demonstration Video
- **Watch Here**: [View the Project Demo](https://drive.google.com/file/d/1Xz_q58SkOkxV3fFuK8Qx0CKWHx-lOLzb/view?usp=sharing)
- **URL**: [(https://drive.google.com/file/d/1Xz_q58SkOkxV3fFuK8Qx0CKWHx-lOLzb/view?usp=sharing)]

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
