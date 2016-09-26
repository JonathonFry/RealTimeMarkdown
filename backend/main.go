package main

import (
	"github.com/olahol/melody"
	"github.com/russross/blackfriday"
	"html/template"
	"net/http"
	"regexp"
)

const split = ">>"

var templates = template.Must(template.ParseFiles("tmpl/edit.html", "tmpl/view.html"))
var validPath = regexp.MustCompile("^/(edit|save|view|ws)/([a-zA-Z0-9-]+)$")

func viewHandler(w http.ResponseWriter, r *http.Request, title string) {
	p, err := loadPage(title)
	if err != nil {
		http.Redirect(w, r, "/edit/"+title, http.StatusFound)
		return
	}

	renderTemplate(w, "view", p)

	output := blackfriday.MarkdownCommon(p.Body)
	w.Write(output)
}

func editHandler(w http.ResponseWriter, r *http.Request, title string) {
	p, err := loadPage(title)
	if err != nil {
		p = &Page{Title: title}
	}

	renderTemplate(w, "edit", p)
}

func saveHandler(w http.ResponseWriter, r *http.Request, title string) {
	body := r.FormValue("body")
	p := &Page{Title: title, Body: []byte(body)}

	err := p.save()
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	http.Redirect(w, r, "/view/"+title, http.StatusFound)
}

func renderTemplate(w http.ResponseWriter, tmpl string, p *Page) {
	err := templates.ExecuteTemplate(w, tmpl+".html", p)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
	}
}

func makeHandler(fn func(http.ResponseWriter, *http.Request, string)) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		// Here we will extract the page title from the Request,
		// and call the provided handler 'fn'

		m := validPath.FindStringSubmatch(r.URL.Path)
		if m == nil {
			http.NotFound(w, r)
			return
		}
		fn(w, r, m[2]) // Title is the second subexpression.
	}
}

func main() {
	m := melody.New()
	http.HandleFunc("/view/", makeHandler(viewHandler))
	http.HandleFunc("/edit/", makeHandler(editHandler))
	http.HandleFunc("/save/", makeHandler(saveHandler))
	http.HandleFunc("/ws/", func(w http.ResponseWriter, r *http.Request) {
		m.HandleRequest(w, r)

		pathSplit := validPath.FindStringSubmatch(r.URL.Path)
		if pathSplit == nil {
			return
		}
		title := pathSplit[2]

		p, err := loadPage(title)
		if err != nil {
			return
		}

		output := blackfriday.MarkdownCommon(p.Body)
		m.Broadcast([]byte(string(p.Body) + split + string(output)))
	})

	m.HandleMessage(func(s *melody.Session, msg []byte) {
		output := blackfriday.MarkdownCommon(msg)
		m.Broadcast([]byte(string(msg) + split + string(output)))
	})

	m.HandleConnect(func(s *melody.Session) {
		pathSplit := validPath.FindStringSubmatch(s.Request.URL.Path)
		if pathSplit == nil {
			return
		}
		title := pathSplit[2]

		p, err := loadPage(title)
		if err != nil {
			return
		}

		output := blackfriday.MarkdownCommon(p.Body)
		m.Broadcast([]byte(string(p.Body) + split + string(output)))
	})

	http.ListenAndServe(":8080", nil)
}
