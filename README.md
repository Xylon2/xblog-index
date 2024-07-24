# Purpose
A simple way to make a blog. Generate the posts as HTML files using whattever
method you want. Then just add them to an edn file and the front-end code will
make a nice index.

# Installation

1. Install dependencies on your workspace:
- babashka
- node

2. First you need to compile:
```npx shadow-cljs compile app```

3. Try the development server:
```npx shadow-cljs watch app```

4. Copy these into your docroot:
- resources/public/cljs/
- add_post.clj
- resources/public/index.html
- resources/public/index.edn

5. Make sure your webserver serves .edn with the right mime-type application/edn

6. Customize index.html with your branding

7. Empty the posts out of index.edn and use the add_post.clj script to add your new posts

# Dev

If you want to do any real dev on it, it is a Shadow-cljs project. For how I put it together see here: https://readtheorg.xylon.me.uk/rr_clojurescript_with_shadow-cljs.html
