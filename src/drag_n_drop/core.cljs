(ns drag-n-drop.core
    (:require [reagent.core :as r :refer [atom]]
              [re-frame.core :refer [subscribe dispatch dispatch-sync]]
              [drag-n-drop.handlers]
              [drag-n-drop.subs]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def PanResponder (.-PanResponder ReactNative))
(def Animated (.-Animated ReactNative))
(def Animated-view (r/adapt-react-class (.-View Animated)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def Alert (.-Alert ReactNative))
(def Dimensions (.-Dimensions ReactNative))

(def circle-radius 36)
(def window-size (js->clj (.get Dimensions "window") :keywordize-keys true))

(defn alert [title]
  (.alert Alert title))

(defn render-draggable []
  [view {:style {:position "absolute"
                     :top (- (/ (:height window-size) 2) circle-radius)
                     :left (- (/ (:width window-size) 2) circle-radius)}}
       [Animated-view {:style {:background-color "#1abc9c"
                               :width (* circle-radius 2)
                               :height (* circle-radius 2)
                               :border-radius circle-radius}}
        [text {:style {:margin-top 25
                       :margin-left 5
                       :margin-right 5
                       :text-align "center"
                       :color "#fff"}}
         "Drag me !!"]]])

(defn app-root []
  (let [greeting (subscribe [:get-greeting])
        pan (r/atom (.ValueXY Animated nil))]
    (fn []
      [view {:style {:flex 1}}
       [view {:style {:height 100
                      :background-color "#2c3e50"}}
        [text {:style {:margin-top 25
                       :margin-left 5
                       :margin-right 5
                       :text-align "center"
                       :color "#fff"}}
         "Drop me here!"]]
       [render-draggable]]
      (println pan))))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "main" #(r/reactify-component app-root)))
