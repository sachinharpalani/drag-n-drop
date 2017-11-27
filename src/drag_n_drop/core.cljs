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

;; JS<->CLJ INTEROP
;; o.myType();
;; (.myType o)

;; a = new MyType()
;; (let [a1 (js/MyType.)
;;       a2 (new js/MyType)])


;; b = new Out.Inner()
;; (let [b1 (js/Out.Inner.)
;;       b2 (new js/Out.Inner)])


(defn render-draggable []
  (let [pan1 (r/atom (new ReactNative.Animated.ValueXY))
        pan2 (r/atom nil)
        state (r/atom 0)]
    (r/create-class
     {:component-did-mount #(do (swap! state inc) (println "Component mounted....."))
      :component-will-mount (fn []
                              (println "Going to mount" (.getLayout @pan1))
                              (let [pr (.create PanResponder
                                                (clj->js {:onStartShouldSetPanResponder #(do (println "onStartShouldSetPanResponder called") true)
                                                          :onMoveShouldSetPanResponder #(do (println "onMoveShouldSetPanResponder called") true)
                                                          :onPanResponderGrant #(do (println "onPanResponderGrant called..") true)
                                                          :onPanResponderMove #(do (println "onPanResponderMove called.." (clj->js (.-x @pan1)))
                                                                                   (.event Animated (clj->js [nil {:dx (.-x @pan1)
                                                                                                                   :dy (.-y @pan1)}])))
                                                          :onPanResponderRelease (fn [e gesture]
                                                                                   (println "onPanResponderRelease called..")
                                                                                   true)
                                                          :onPanResponderTerminate #(do (println "onPanResponderTerminate called..") true)}))]
                                (reset! pan2 pr)
                                (println "Going to mount 2" )))
      :display-name "Circle"
      :reagent-render (fn []
                        (println "@@@@" @state)
                        (println "Pan1--->>>>>>>" @pan1)
                        (println (js->clj @pan2))
                        (println "Pan Handlers----->>>>>>" (js->clj (.-panHandlers @pan2)))
                        [view {:style {:position "absolute"
                                       ; :flex 1
                                       :border-width 2
                                       :border-color "red"
                                       :top (- (/ (:height window-size) 2) circle-radius)
                                       :left (- (/ (:width window-size) 2) circle-radius)}}
                         [Animated-view (merge (js->clj (.-panHandlers @pan2)
                                                        :keywordize-keys true)
                                               {:style (merge (js->clj (.getLayout @pan1)
                                                                       :keywordize-keys true)
                                                              {:background-color "#1abc9c"
                                                               :width (* circle-radius 2)
                                                               :height (* circle-radius 2)
                                                               :border-radius circle-radius})})

                          [text {:style {:margin-top 25
                                         :margin-left 5
                                         :margin-right 5
                                         :text-align "center"
                                         :color "#fff"}}
                           "Drag me !!"]]])})))

#_(def pan-responder (.-PanResponder ReactNative))

#_(defn cirlce
  [circle-radius]
  (let [radius circle-radius
        *pan   (atom nil)]
    (r/create-class
     {:component-did-mount
      #(println "Component mounted..")
      :component-will-mount
      (fn []
        (println "Going to mount..")
        (let [pr (.create pan-responder (clj->js {:onStartShouldSetPanResponder #(do (println "onStartShouldSetPanResponder called") true)
                                                  :onMoveShouldSetPanResponder #(do (println "onMoveShouldSetPanResponder called") true)

                                                  :onPanResponderGrant #(println "onPanResponderGrant called..")
                                                  :onPanResponderMove #(println "onPanResponderMove called..")
                                                  :onPanResponderRelease #(println "onPanResponderRelease called..")
                                                  :onPanResponderTerminate #(println "onPanResponderTerminate called..")}))]
          (reset! *pan pr)))
      :display-name "Call circle"
      :reagent-render
      (fn [circle-radius]
        (println "in RENDER: pan handlers: " (js->clj (.-panHandlers @*pan)))
        [view
         (merge (js->clj (.-panHandlers @*pan))
                {:style {:width  (* 2 circle-radius)
                         :height (* 2 circle-radius)
                         :position "absolute"
                         :left 0
                         :top 0
                         :background-color "green"
                         :border-width 1
                         :border-radius circle-radius}})])})))

(defn app-root []
  (let [greeting (subscribe [:get-greeting])]
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
       [render-draggable]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "main" #(r/reactify-component app-root)))
