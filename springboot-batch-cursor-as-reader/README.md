# Hey there! 👋

Just a quick note about using `JdbcCursorItemReader` in your project:

### What’s the deal with `JdbcCursorItemReader`?
Since it’s not managed (no fancy persistence context here), it doesn’t play super well with JPA stuff like `JpaItemWriter` or `JpaRepository.saveAll`. At first glance, they might seem to work together just fine—but hold up! If you turn on `show-sql` (set it to `true`), you’ll notice a bunch of extra `SELECT` queries popping up. That’s not great, especially if you’re aiming for speedy batch inserts. It kinda defeats the whole point!

### So, what should you do?
If you’re using `JdbcCursorItemReader`, do not pair with JpaWriter or JpaRepository. Use JdbcBatchItemWriter or 
if you are working on single table, you may just create a custom query to your JpaRepository to update specific columns by ids.

### TL;DR
- Use `JdbcCursorItemReader`? Stick to custom update queries.
- Use JdbcBatchItemWriter if you need to insert child rows.

Happy coding! 🚀
