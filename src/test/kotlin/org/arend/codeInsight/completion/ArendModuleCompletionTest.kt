package org.arend.codeInsight.completion

class ArendModuleCompletionTest : ArendCompletionTestBase() {
    fun `test module name completion`() = doSingleCompletionMultiflie(
            """
                --! Main.ard
                \import My{-caret-}

                --! MyModule.ard
                -- empty
            """,
            """
                \import MyModule
            """
    )

    fun `test directory name completion`() = doSingleCompletionMultiflie(
            """
                --! Main.ard
                \import Dir{-caret-}

                --! Directory/MyModule.ard
                -- empty
            """,
            """
                \import Directory{-caret-}
            """
    )

    fun `test module name completion subdirectory`() = doSingleCompletionMultiflie(
            """
                --! Main.ard
                \import Directory.My{-caret-}

                --! Directory/MyModule.ard
                -- empty
            """,
            """
                \import Directory.MyModule{-caret-}
            """
    )
}