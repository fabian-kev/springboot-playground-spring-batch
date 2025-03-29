# Hey there! 👋

Just a quick note about using `JdbcCursorItemReader` in your project:

### What’s the deal with `JdbcCursorItemReader`?
Since it’s not managed (no fancy persistence context here), it doesn’t play super well with JPA stuff like `JpaItemWriter` or `JpaRepository.saveAll`. At first glance, they might seem to work together just fine—but hold up! If you turn on `show-sql` (set it to `true`), you’ll notice a bunch of extra `SELECT` queries popping up. That’s not great, especially if you’re aiming for speedy batch inserts. It kinda defeats the whole point!

### So, what should you do?
If you’re using `JdbcCursorItemReader`, skip `saveAll` and go with a custom update query instead. It’ll keep things running smoothly and avoid those pesky extra queries.

### When *not* to use `JdbcCursorItemReader`?
Picture this: you’ve got a 1-to-many relationship (like a parent with lots of kids), and along the way, you need to insert those child rows. For that, `saveAll` is awesome because it batches your `INSERT` statements. But if you pair it with `JdbcCursorItemReader`, you’ll end up with those N `SELECT` queries again. Not cool!

### TL;DR
- Use `JdbcCursorItemReader`? Stick to custom update queries.
- Need to batch-insert child rows with `saveAll`? Pick a different reader that’s managed by JPA.

Happy coding! 🚀
