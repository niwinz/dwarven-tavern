(ns dwarven-tavern.client.views
  (:require [sablono.core :as html :refer-macros [html]]
            [rum.core :as rum]
            [cats.labs.lens :as l]
            [cuerdas.core :as str]
            [dwarven-tavern.client.router :as r]
            [dwarven-tavern.client.game :as g]
            [dwarven-tavern.client.rstore :as rs]
            [dwarven-tavern.client.state :as s]
            [dwarven-tavern.client.postal :as p]
            [dwarven-tavern.client.util :as util]))

;; (def ^:static sprites
;;   {:team1  "url(/images/team1.png)"
;;    :team2  "url(/images/team2.png)"
;;    :barrel "url(/images/barrel.png)"})

;; (def ^:static dwarf-animation
;;   {:north [{ :x 0  :y 32 :width 33 :height 32}
;;            { :x 33 :y 32 :width 33 :height 32}
;;            { :x 66 :y 32 :width 33 :height 32}]
;;    :east  [{ :x 0  :y 64 :width 33 :height 32}
;;            { :x 33 :y 64 :width 33 :height 32}
;;            { :x 66 :y 64 :width 33 :height 32}]
;;    :south [{ :x 0  :y 0  :width 33 :height 33}
;;            { :x 33 :y 0  :width 33 :height 32}
;;            { :x 66 :y 0  :width 33 :height 32}]
;;    :west  [{ :x 0  :y 96 :width 33 :height 32}
;;            { :x 33 :y 96 :width 33 :height 32}
;;            { :x 66 :y 96 :width 33 :height 32}]})

;; (def ^:static barrel-animation
;;   {:north [{ :x 32 :y 0 :width 32 :height 32}]
;;    :east  [{ :x 0  :y 0 :width 32 :height 32}]
;;    :south [{ :x 32 :y 0 :width 32 :height 32}]
;;    :west  [{ :x 0  :y 0 :width 32 :height 32}]})

;; (defonce heartbeat (atom 0))
;; (defonce interval-timer
;;   (js/setInterval (fn [] (swap! heartbeat inc)) 200))

;; (defn render-barrel [direction]
;;   (let [sprite-url (:barrel sprites)
;;         sprite (-> barrel-animation direction first)
;;         {:keys [x y width height]} sprite]
;;     [:.barrel {:style {:background-image sprite-url
;;                        :width (str width "px")
;;                        :height (str height "px")
;;                        :background-position (str x "px " y "px")
;;                        :transform "rotateZ(180deg)"}}]))

;; (defn render-dwarf [team animation idx]
;;   (let [sprite-url (team sprites)
;;         sprite (-> dwarf-animation animation (nth idx))
;;         {:keys [x y width height]} sprite]
;;     [:.dwarf.team1 {:style {:background-image sprite-url
;;                             :width (str width "px")
;;                             :height (str height "px")
;;                             :background-position (str x "px " y "px")}}]))

;; (def north-keys? #{87 38})
;; (def south-keys? #{83 40})
;; (def west-keys? #{65 37})
;; (def east-keys? #{68 39})

;; (defn movement
;;   [signal room]
;;   (fn [ev]
;;     (.preventDefault ev)
;;     (let [key (.-keyCode ev)]
;;       (cond
;;         (north-keys? key)
;;         (signal [:move {:room room
;;                         :direction :north}])

;;         (south-keys? key)
;;         (signal [:move {:room room
;;                         :direction :south}])

;;         (west-keys? key)
;;         (signal [:move {:room room
;;                         :direction :west}])

;;         (east-keys? key)
;;         (signal [:move {:room room
;;                         :direction :east}])))))

;; (defn game-grid
;;   [own]
;;   (let [{:keys [width height players barrel] } (->  own :rum/props :state deref :current-game)
;;         signal (get-in own [:rum/props :signal])
;;         {[posx-barrel posy-barrel] :pos} barrel
;;         dir-barrel :east]
;;     [:table.grid
;;      {:tab-index 0
;;       :on-key-down (movement signal :foo)} ;; TODO: pass actual room instead of `:foo`
;;      [:tbody
;;       (for [row (range 0 height)]
;;         [:tr {:style
;;               (condp = row
;;                 0            {:background-color "rgba(255, 0, 0, 0.22)"}
;;                 (- height 1) {:background-color "rgba(255, 255, 0, 0.22)"}
;;                 {})}
;;          (for [column (range 0 width)]
;;            [:td
;;             (for [[player-id {:keys [pos team dir]}] players]
;;               (when (and (= row (first pos)) (= column (second pos)))
;;                 (render-dwarf team dir (mod (rum/react heartbeat) 3)))
;;               )
;;             (when (and (= row posy-barrel) (= column posx-barrel))
;;               (render-barrel dir-barrel))

;;             ])])]]))


;; (defn render-room-list
;;   [own]
;;   (let [signal (-> own :rum/props :signal)
;;         router (-> own :rum/props :router)
;;         {:keys [room-list]} (-> own :rum/props :state deref)
;;         player-id (-> own :rum/props :state deref :player)]
;;     [:.container.room-list
;;      [:img#logo {:src "/images/tavern-logo.png"}]
;;      [:.room-list-container
;;       [:div.room-list-header
;;        [:h2.room-list-title "Available games"]
;;        [:a.new-game {:href "#"
;;                      :on-click (fn [e]
;;                                  (.preventDefault e)
;;                                  (let [room-id (keyword (str "room_" (rand-int 100000000)))]
;;                                    (signal [:join-room room-id])))}
;;         "New game!"]]
;;       [:a.help {:href "#/help"} "How to play?"]
;;       [:ul
;;        (for [{:keys [id players status]} room-list]
;;          [:li
;;           [:div.room-element
;;            [:span.room-element-name (name id)]
;;            [:span.room-element-room (str "(" players "/" 2 ")")]
;;            (when (= status :pending)
;;              [:a.join {:href (str "#/game/" (name id))
;;                        :on-click #(signal [:join-room id])} "Join"])]])]
;;       ]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Game Page
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; (defn render-game
;;   [own]
;;   (let [state (rum/react s/state)



;; {:keys [total-time time-progress team1 team2]} (-> own :rum/props :state deref :current-game)
;;         total-time 0
;;         time-progress 0

;;         score-t1 0
;;         score-t2 0
;;         ]
;;     [:.container.game
;;      [:img#logo {:src "/images/tavern-logo.png"}]
;;      [:.turnprogress
;;       [:span.time-label "Turn time"]
;;       [:.time-slider [:.progress {:style {"width" (str (* 100 (/ time-progress total-time)) "%")}}]]
;;       [:.time-counter time-progress]]
;;      [:.scoreboard
;;       [:.team.team1
;;        (for [id team1]
;;          [:span.name (name id)])
;;        [:span.score score-t1]]
;;       [:.vs "VS"]
;;       [:.team.team2
;;        (for [id team2]
;;          [:span.name (name id)])
;;        [:span.score score-t2]]]
;;      (game-grid own)]))

;; (def game
;;   (util/component
;;    {:render game-render
;;     :name "game"
;;     :mixins [rum/reactive]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Rooms Page
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn room-list-init
  [own]
  ;; When user is not set, just redirect
  ;; to the home page
  (when-not (:player @s/state)
    (r/go :home))
  own)

(defn room-item-render
  [{:keys [id status players] :as room}]
  (letfn [(on-join-clicked [e]
            (util/prevent-default e)
            (rs/emit! (g/join-game id)
                      (r/navigate :game {:id id})))]
    (html
     [:li {:key (str "room-" id)}
      [:div.room-element
       [:span.room-element-name (name id)]
       [:span.room-element-room (str "(" players "/" 2 ")")]
       (when (= status :pending)
         [:a.join {:href "#" :on-click on-join-clicked} "Join"])]])))

;; Declare a specific state vision using lenses
;; for the room list page.
(def roomlist-state
  (as-> (l/select-keys [:rooms :rooms-by-id :player]) $
    (l/focus-atom $ s/state)))

(defn room-list-render
  [own]
  (let [state (rum/react roomlist-state)
        player (:player state)
        rooms (:rooms state)
        on-click #(do (util/prevent-default %)
                      (rs/emit! (g/join-game)))]
    (html
     [:.container.room-list
      [:img#logo {:src "/images/tavern-logo.png"}]
      [:.room-list-container
       [:div.room-list-header
        [:h2.room-list-title "Available games"]
        [:a.new-game {:href "#" :on-click on-click} "New game!"]]
       [:a.help {:href "#/help"} "How to play?"]
       [:ul
        (for [id rooms]
          (let [room (get-in state [:rooms-by-id id])]
            (room-item-render room)))]]])))

(def room-list
  (util/component
   {:render room-list-render
    :init room-list-init
    :name "room-list"
    :mixins [rum/reactive]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Help Page
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn help-render
  [_]
  (html
   [:.container.help
    [:section.help1 {:key "1"}
     [:h3 "Game objective"]
     [:.content
      [:img {:src "/images/rule1.png"}]
      [:p  "You control one dwarf and your objective is to pick the barrel and
           bring it to the taver zone for your team."]]]
    [:section.help2 {:key "2"}
     [:h3 "Taking turns"]
     [:.content
      [:img {:src "/images/rule2.png"}]
      [:p  "You control one dwarf and your objective is to pick the barrel and
           bring it to the taver zone for your team."]]]
    [:section.help3 {:key "3"}
     [:h3 "Collisions"]
     [:.content
      [:img {:src "/images/rule3.png"}]
      [:p  "You control one dwarf and your objective is to pick the barrel and
           bring it to the taver zone for your team."]]]]))

(def help
  (util/component
   {:render help-render
    :name "help"
    :mixins [rum/static]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Home Page
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn home-render
  [own]
  (letfn [(on-click [e]
            (util/prevent-default e)
            (let [value (str/trim (util/ref-value own "nick-input"))]
              (when-not (str/empty? value)
                (rs/emit! (g/set-player value)
                          (g/load-rooms)
                          (r/navigate :rooms)))))]
    (html
     [:.container.home
      [:div.form-wrapper
       [:div.image-wrapper
        [:img#logo {:src "/images/tavern-logo.png"}]]
       [:input {:ref "nick-input"
                :auto-focus "autofocus"
                :placeholder "What's yar name?"
                :pattern "^[a-zA-Z0-9]+$"}]
       [:a.join-game {:href "#/rooms" :on-click on-click} "To Arms!"]]])))

(def home
  (util/component
   {:render home-render
    :name "home"
    :mixins [rum/static]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Root
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def root-state
  (as-> (l/select-keys [:location :location-params]) $
    (l/focus-atom $ s/state)))

(defn root-render
  [_]
  ;; Only reacts on url changes thans to the
  ;; lenses focused main state.
  (let [state (rum/react root-state)]
    (html
     (case (:location state)
       :home (home)
       :rooms (room-list)
       :help (help)
       :game  (help)
       nil))))

(def root
  (util/component
   {:render root-render
    :name "root"
    :mixins [rum/reactive]}))

