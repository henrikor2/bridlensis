a = "hello"

R0 = "!" ; predefined variables $R0..9 can be used
       ; $0..9 are out of the game

b = a + " world" + R0 ; <-- "hello world!"

p = 1

pre = 2

    DetailPrint(pre) ; <-- "1" (or "2" depending on NSIS compiler)
	
	c= \ 
        a + b + r0    ; <-- "hellohello world!!"
    
DetailPrint ( a + r0 +"!")

; eof