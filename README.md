ontop-build-dependencies
==================

Some dependencies for building ontop distributions

### How to update this repository

```terminal
$ git add file/to/be/added
$ git ci -m 'add a new file'
$ cd ..
# now you are in the root directory of ontop
$ git submodule update --remote
$ git ci -am 'ontop-build-dependencies sub-module updated'
```
