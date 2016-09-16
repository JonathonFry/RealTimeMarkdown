package main

import (
	"github.com/gin-gonic/gin"
	"github.com/olahol/melody"
	"github.com/russross/blackfriday"
	"net/http"
)

func main() {
	gin.SetMode(gin.ReleaseMode)
	r := gin.Default()
	m := melody.New()
	var buffer []byte

	r.GET("/", func(c *gin.Context) {
		http.ServeFile(c.Writer, c.Request, "index.html")
	})

	r.GET("/ws", func(c *gin.Context) {
		m.HandleRequest(c.Writer, c.Request)
		m.Broadcast(buffer)
	})

	m.HandleMessage(func(s *melody.Session, msg []byte) {
		output := blackfriday.MarkdownBasic(msg)
		buffer = []byte(string(msg) + ">>" + string(output))
		m.Broadcast(buffer)
	})

	m.HandleConnect(func(s *melody.Session) {
		if buffer != nil {
			m.Broadcast(buffer)
		}
	})

	r.Run(":5432")
}
