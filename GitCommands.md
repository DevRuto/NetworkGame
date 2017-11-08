# Git Commands
## Git binaries @ https://git-scm.com/
### How to get the project
```bash
git clone https://github.com/nullruto/NetworkGame
```

### How to add files to be watched by the repo
*Files are not automatically added into the local repo, new files need to be added manually*

**Files in [gitignore](https://github.com/nullruto/NetworkGame/.gitignore) are ignored**
```bash
# Add multiple files manually
git add <file1> <file2>
# Add all possible files in current directory to repo 
git add .
```

### How to push(upload) files to the online repo
```bash
# Add a commit (a record of changes) with a <short> message (required)
git commit -m "<message>"
# Push the commit to the online repo
git push
# If it warns you about no defaults use:
git push origin dev
# git push <local repo branch> <remote repo branch>
```

### How to pull(download) files to synchronize with the online repo
```bash
git pull
```
Sometimes there are merge conflicts. It's easier to google the best way to resolve this.
Most of the time you will end up manually merging it.
