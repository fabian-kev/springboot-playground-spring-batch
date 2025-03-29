

Since `JdbcCursorItemReader` is not managed, this should not pair with JPA which rely on persistent context.
When you combine `JdbcCursorItemReader` and `JpaItemWriter` or `JpaRepository.saveAll` they seem they work completely fine, but
if you enable `show-sql` to true, you will see N select queries are being generated which is super bad and it defeats the purpose batch inserts.

If you are going to use JdbcCursorItemReader make sure to use custom update query and not `saveAll`.


When not use `JdbcCursorItemReader` or custom unmanaged reader?
In a scenario where you have 1 to many relationship and you along the process you have to insert child rows.
This requirement needs `saveAll` to batch the insert SQL but when you use `JdbcCursorItemReader` this will generate N select queries.

