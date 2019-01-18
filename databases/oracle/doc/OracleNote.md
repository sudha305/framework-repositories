# Oracle笔记

## 概述
目前数据库大体上分为两类，一类是关系型数据库，一类是非关系型数据库。其实在业界中，大体上可以分为三大类软件公司：
- 互联网行业（如电商、P2P、O2O、互联网金融等）
- 传统领域软件行业（如交通、气象、电信、银行等某个领域的软件需求）
- 产品软件行业（如医疗、军工、OA、企业级管理系统、小行业等第三方的软件需求）

但是无论任何软件公司，他们都离不开数据库，根据需求的不同，选择相应的数据库作为存储。有人说一般互联网行业都会选择使用MySQL数据库，原因很简单，开源且成本低，其实一般的大型互联公司，往往不是只单单使用一种数据库。可能是多种混合这使用，比如互联网金融公司的核心数据库一般采用Oracle或者DB2，为了对安全做保证，外围数据库可能使用MySQL，一些即时的信息或安全度不是特别高的信息可能会使用缓存。所以，使用什么数据库是要看具体的需求而定的，并不能一概而论。

### 数据库类型
对于关系型数据库，不在强调具体细节，关系型数据库的最大特点就是事务的一致性：传统的关系型数据库读写操作都是事务的，具有ACID的特点，这个特性使得关系型数据库可以用于几乎所有对一致性有要求的系统中，如典型的银行系统。

相反地，关系型数据库为了维护一致性所付出的巨大代价就是其读写性能比较差，而像微博、facebook这类SNS的应用，对并发读写能力要求极高，关系型数据库已经无法应付，所以会采用一系列的非关系型数据库，或者缓存来提供系统的性能，尤其是web2.0的兴起。

关系型数据库和非关系型数据库的区别？

最终无论数据怎么做处理都要有数据落地（入库），一般我们会存储在关系型数据库中，所以无论关系型、非关系型数据库，我们都要掌握它们的使用和应用范围、场景，一个好的数据设计，往往可以规避很多复杂的逻辑和后期的不可预见性，所以一般设计数据库的工程师在企业里都是很重要的核心成员。

### 关系型数据库
所接触了解最多的是关系型数据库，如Oracle、MySQL、DB2、SQLServer等，这些数据库你不可能每种都精通，但是如果你学会了Oracle，那么其它的数据库学习起来就相对的容易很多。在关系数据中，Oracle也是最复杂、最博大精深的。

## 1.1 数据库语言

数据操作语言：DML(data manipulation language)
- `select insert update delete merge`

数据定义语言：DDL(data definition language)
- `create alter drop truncate`

事务控制语言：TCL(transaction control language)
- `commit rollback savepoint`

数据控制语言：DCL(Data Control Language )
- `grant revoke`

## 1.2 数据类型
SQL的数据类型：1字符型，2数值型，3日期型，4大对象型
- `char`：固定字符，最长2000个
- `varchar2`：可变长，最长4000，最小值1
- `number`类型
- `date`、`timestamp`
- `clob`：存储单字节数据，文本数据
- `blob`：存储二进制数据

## 1.3 常用数据类型转换
常用转换：`date`、`char`、`number`三者之间经常进行转换
```
SELECT TO_DATE('2015-08-19','YYYY-MM-DD') AS A_DAY FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD') AS TODAY FROM DUAL;
SELECT TO_TIMESTAMP('2015-08-19 17:40:32.11', 'YYYY-MM-DD HH24:MI:SS.FF') AS A_DAY FROM DUAL;
SELECT TO_CHAR(SYSTIMESTAMP, 'YYYY-MM-DD HH24:MI:SS.FF') AS TODAY FROM DUAL;
SELECT TO_NUMBER(REPLACE(TO_CHAR(SYSDATE, 'YYYY-MM-DD'),'-'))FROM DUAL;
```
[Oracle中TO_DATE TO_CHAR格式](OracleDateChar.md)：`SELECT TO_CHAR(SYSDATE, '参数') FROM DUAL;`

## 1.4 常用函数
`decode`函数：`SELECT DEPTNO,DECODE(DEPTNO,10,'部门1',20,'部门2','部门3') FROM EMP;`

`case`表达式：`SELECT DEPTNO,CASE WHEN DEPTNO=10 THEN '部门1' WHEN DEPTNO=20 THEN '部门2' ELSE '部门3' END FROM EMP;`

学习使用函数：`TRUNC`、`ADD_MONTHS`、`MONTH_BETWEEN`、`LAST_DAY`、`REPLACE`、`SUBSTR`、`CONCAT`、`ABS`、`ROUND`

## 1.5 分组
组函数：`sum min max avg count`

分组特性：`group by ... having`

1. 查询每个部门的平均薪水之后，显示部门平均薪水大于2000的部门编号和其平均薪水。答案：`SELECT DEPTNO, AVG(SAL) FROM EMP GROUP BY DEPTNO HAVING AVG(SAL) > 2000;`
1. 查询每个部门薪水大于2000，且平均薪水大于3000的部门编号和其平均薪水。答案：`SELECT DEPTNO, AVG(SAL) FROM EMP WHERE SAL > 2000 GROUP BY DEPTNO HAVING AVG(SAL) > 3000;`
1. 查询每个部门的薪水和，再算出所有部门的薪水平均值。答案：`SELECT DEPTNO, SUM(DEPTNO), AVG(SAL) FROM EMP GROUP BY DEPTNO;`

## 1.6 集合查询操作
1. 交叉连接（笛卡尔积）
1. 等值、非等值连接
1. 内连接
1. 外连接（左外、右外、全连接）
1. 自连接
1. 自然连接（隐含链接条件，自动匹配链接自动）
1. 集合运算
    - `union`（求合集，重复记录只显示一次）
    - `union all`（求合集，显示所有记录信息）
    - `intersect`（求交集，显示公共的数据部分）
    - `minus`（集合相减，哪个表在前面以哪个表的数据为主）

```
CREATE TABLE A (ID INT, NAME VARCHAR2(10));
CREATE TABLE B (ID INT, NAME VARCHAR2(10));
INSERT INTO A VALUES(1, '张三');
INSERT INTO A VALUES(2, '李四');
INSERT INTO B VALUES(2, '李四');
INSERT INTO B VALUES(3, '王五');
SELECT * FROM A;
SELECT * FROM B;
-- INTERSECT
SELECT * FROM A INTERSECT SELECT * FROM B;
-- MINUS（A表为主）
SELECT * FROM A MINUS SELECT * FROM B;
-- MINUS（B表为主）
SELECT * FROM B MINUS SELECT * FROM A;
```


## References
- Oracle