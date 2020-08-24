package utils

import groovy.io.FileType

class FileGetter {

    def GetFile(FileLocation){
        def FileList = []
        def dir = new File(FileLocation)
        dir.eachFileRecurse (FileType.FILES) { file ->
            FileList << file
        }
        return FileList
    }
}