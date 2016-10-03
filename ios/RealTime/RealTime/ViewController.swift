//
//  ViewController.swift
//  RealTime
//
//  Created by Jonathon Fry on 15/09/2016.
//  Copyright © 2016 Jonathon Fry. All rights reserved.
//

import UIKit
import Starscream


class ViewController: UIViewController {
    
    @IBOutlet var label : UILabel!
    @IBOutlet weak var textView: UITextView!
    
    var socket : WebSocket!


    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        socket = WebSocket(url: URL(string: "ws://localhost:3004/realtimemarkdown/ws/")!)
        socket.delegate = self
        socket.connect()
        
        textView.delegate = self
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
}

extension ViewController: UITextViewDelegate {
    
    func textViewDidChange(_ textView: UITextView) { //Handle the text changes here
        guard socket != nil else {
            return
        }
        socket.write(string: textView.text!)
    }
}


// MARK: - WebSocketDelegate
extension ViewController: WebSocketDelegate {
    
    func websocketDidConnect(socket: WebSocket) {
        print("websocket is connected")
    }
    
    func websocketDidDisconnect(socket: WebSocket, error: NSError?){
        print("websocket is disconnected" + (error?.description)!)
    }
    func websocketDidReceiveMessage(socket: WebSocket, text: String) {
        let parts : [String] = text.components(separatedBy: ">>")
        guard parts.count > 1 else {
            return
        }
        let raw : String = parts[0]
        let html : String = parts[1]
        
        
        let attrStr = try! NSAttributedString(
            data: html.data(using: String.Encoding.unicode, allowLossyConversion: true)!,
            options: [ NSDocumentTypeDocumentAttribute: NSHTMLTextDocumentType],
            documentAttributes: nil)
        
        self.label.attributedText = attrStr
        
        if(raw != self.textView.text){
            self.textView.text = raw
        }
        
        print("websocket did receieve message \(text)")
    }
    func websocketDidReceiveData(socket: WebSocket, data: Data){
        print("websocket did receieve data")
    }
}

