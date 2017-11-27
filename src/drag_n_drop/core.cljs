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

(def smiley (js/require "./assets/images/smiley.gif"))

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

;; This was test try using form-3, works now but no need for form3 so refactored to simple one
;; (defn render-draggable []
;;   (let [pan1 (r/atom (new ReactNative.Animated.ValueXY))
;;         pan2 (r/atom nil)
;;         state (r/atom 0)]
;;     (r/create-class
;;      {:component-did-mount (fn []
;;                              (println "Component mounted....."))
;;       :component-will-mount (fn []
;;                               (println "Going to mount" (.getLayout @pan1))
;;                               (let [pr (.create PanResponder
;;                                                 (clj->js {:onStartShouldSetPanResponder #(do (println "onStartShouldSetPanResponder called") true)
;;                                                           :onMoveShouldSetPanResponder #(do (println "onMoveShouldSetPanResponder called") true)
;;                                                           :onPanResponderGrant #(do (println "onPanResponderGrant called..") true)
;;                                                           :onPanResponderMove (.event Animated (clj->js [nil {:dx (.-x @pan1)
;;                                                                                                               :dy (.-y @pan1)}]))
;;                                                           :onPanResponderRelease (fn [e gesture]
;;                                                                                    (println "onPanResponderRelease called..")
;;                                                                                    true)
;;                                                           :onPanResponderTerminate #(do (println "onPanResponderTerminate called..") true)}))]
;;                                 (reset! pan2 pr)
;;                                 (println "Going to mount 2" )))

;;       :display-name "Circle"
;;       :reagent-render (fn []
;;                         (println "State !!!!!!!" )
;;                         (println "Pan1---------" (js->clj @pan1))
;;                         (println "Pan2+++++++++" (js->clj @pan2 :keywordize-keys true))
;;                         (println "Pan Handlers ************" (js->clj (.-panHandlers @pan2) :keywordize-keys true))
;;                         (println "getLayout %%%%%%%%%" (js->clj (.getLayout @pan1)))
;;                         [view {:style {:position "absolute"
;;                                        :top (- (/ (:height window-size) 2) circle-radius)
;;                                        :left (- (/ (:width window-size) 2) circle-radius)}}
;;                          [Animated-view (merge (js->clj (.-panHandlers @pan2)
;;                                                         :keywordize-keys true)
;;                                                {:style (merge (js->clj (.getLayout @pan1)
;;                                                                        :keywordize-keys true)
;;                                                               {:background-color "#1abc9c"
;;                                                                :width (* circle-radius 2)
;;                                                                :height (* circle-radius 2)
;;                                                                :border-radius circle-radius})})

;;                           [text {:style {:margin-left 5
;;                                          :margin-right 5
;;                                          :text-align "center"
;;                                          :color "#fff"}}
;;                            "Drag me !!"]]])})))

(defn render-draggable [isDropZone]
  (let [showDraggable? (r/atom true)
        pan1 (new ReactNative.Animated.ValueXY)
        pan2 (.create PanResponder
                        (clj->js {:onStartShouldSetPanResponder #(do (println "onStartShouldSetPanResponder called") true)
                                  :onMoveShouldSetPanResponder #(do (println "onMoveShouldSetPanResponder called") true)
                                  :onPanResponderGrant #(do (println "onPanResponderGrant called..") true)
                                  :onPanResponderMove (.event Animated (clj->js [nil {:dx (.-x pan1)
                                                                                      :dy (.-y pan1)}]))
                                  :onPanResponderRelease (fn [e gesture]
                                                           (println "onPanResponderRelease called..")
                                                           (println (isDropZone gesture))
                                                           (if (isDropZone gesture)
                                                             (do (reset! showDraggable? false)
                                                               (js/alert "Game over"))
                                                             (.start (.spring Animated pan1 (clj->js {:toValue {:x 0 :y 0}})))))
                                  :onPanResponderTerminate #(do (println "onPanResponderTerminate called..") true)}))]
    (fn []
      (if @showDraggable?
        [view {:style {:position "absolute"
                       :top (- (/ (:height window-size) 2) circle-radius)
                       :left (- (/ (:width window-size) 2) circle-radius)}}
         [Animated-view (merge (js->clj (.-panHandlers pan2)
                                        :keywordize-keys true)
                               {:style (merge (js->clj (.getLayout pan1)
                                                       :keywordize-keys true)
                                              {:background-color "#1abc9c"
                                               :width (* circle-radius 2)
                                               :height (* circle-radius 2)
                                               :border-radius circle-radius})})

          [text {:style {:margin-left 5
                         :margin-right 5
                         :text-align "center"
                         :color "#fff"}}
           "Drag me !!"]]]
        [view]))))

(defn app-root []
  (let [greeting (subscribe [:get-greeting])

        dropZoneValues (r/atom nil)
        isDropZone (fn [gesture]
                     (let [touch-y (.-moveY gesture)
                           drop-zone-y (.-y @dropZoneValues)
                           drop-zone-ht 100 #_(.-width @dropZoneValues)]
                       (println "touch-y" touch-y)
                       (and (> touch-y  drop-zone-y) (< touch-y (+ drop-zone-ht drop-zone-y)))))]
    (fn []
      [view {:on-layout (fn [e] (let [vals (.-layout (.-nativeEvent e))]
                                  (println vals)
                                  (reset! dropZoneValues vals)))
             :style {:flex 1}}
       [view {:style {:height 100
                      :background-color "#2c3e50"}}
        [text {:style {:margin-top 25
                       :margin-left 5
                       :margin-right 5
                       :text-align "center"
                       :color "#fff"}}
         "Drop me here!"]]
          [render-draggable isDropZone]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "main" #(r/reactify-component app-root)))
