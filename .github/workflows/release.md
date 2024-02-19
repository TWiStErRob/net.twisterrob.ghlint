# How to test full release process?

1. Have a change that needs testing on a branch.
2. Create a new repository, let's call this `ghlint-test`.
3. Add the fork as a remote to the working copy original clone.
   ```shell
   git remote add test https://github.com/TWiStErRob/ghlint-test.git
   ```
4. Push the working branch as main.
   ```shell
   git push test HEAD:main -f
   ```
5. Execute the [release process](../../docs/RELEASING.md) in the clone repo.

After making adjustments, repeat from step 4.

# How to test only release-prepare.yml?

1. Have a change that needs testing on a branch.
2. Create a new repository, let's call this `ghlint-test`.
3. Add the fork as a remote to the working copy original clone.
   ```shell
   git remote add test https://github.com/TWiStErRob/ghlint-test.git
   ```
4. Push the working branch as main.
   ```shell
   git push test HEAD:main -f
   ```
5. In the clone of the repository trigger the workflow.
   ```shell
   gh workflow run --repo TWiStErRob/ghlint-test release-prepare.yml
   ```
   Check the [running workflow](https://github.com/TWiStErRob/ghlint-test/actions/workflows/release-prepare.yml).

After making adjustments, repeat from step 4.

# How to test only release-publish.yml?

1. Have a change that needs testing on a branch.
2. Create a new repository, let's call this `ghlint-test`.
3. Add the fork as a remote to the working copy original clone.
   ```shell
   git remote add test https://github.com/TWiStErRob/ghlint-test.git
   ```
4. Add a commit to the working branch.
   ```shell
   git commit --allow-empty -m "Release vTest"
   ```
5. Push the working branch as main.
   ```shell
   git push test HEAD:main -f
   ```

After making adjustments, repeat from step 4.
