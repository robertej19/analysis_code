package mainutils

class Printer {

    def printer(string,override){
        def k = 0
        if(k==1){
            println("\n"+string+"\n")
            if(override==2){
                println(string)
            }
        }
        if(k==0){
            if(override==1){
                println(string+"\n")
            }
            if(override==2){
                println(string)
            }
        }
    }
}