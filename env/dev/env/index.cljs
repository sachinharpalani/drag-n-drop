(ns env.index
  (:require [env.dev :as dev]))

;; undo main.js goog preamble hack
(set! js/window.goog js/undefined)

(-> (js/require "figwheel-bridge")
    (.withModules #js {"react-native" (js/require "react-native"), "./assets/images/smiley.gif" (js/require "../../../assets/images/smiley.gif"), "react" (js/require "react"), "expo" (js/require "expo"), "create-react-class" (js/require "create-react-class"), "./assets/song.mp3" (js/require "../../../assets/song.mp3"), "./assets/images/cljs.png" (js/require "../../../assets/images/cljs.png"), "./assets/icons/loading.png" (js/require "../../../assets/icons/loading.png"), "./assets/icons/app.png" (js/require "../../../assets/icons/app.png")}
)
    (.start "main" "expo" "192.168.0.179"))
