# Installation

1. First you need to compile:
```npx shadow-cljs compile app```

2. Try the development server:
```npx shadow-cljs watch app```

3. Copy these into your docroot:
- resources/public/cljs/
- add_post.clj
- resources/public/index.html
- resources/public/index.edn

4. Make sure your webserver serves .edn with the right mime-type application/edn

5. Customize index.html with your branding

6. Empty the posts out of index.edn and use the add_post.clj script to add your new posts

# Dev

If you want to do any real dev on it, it is a Shadow-cljs project. For how I put it together see here: https://readtheorg.xylon.me.uk/rr_clojurescript_with_shadow-cljs.html
