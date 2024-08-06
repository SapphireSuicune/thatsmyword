(ns tmw.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [re-frame.core :refer [dispatch subscribe reg-sub reg-event-fx]]
      [kee-frame.core :as k]))

;; -------------------------
;; Views

(reg-sub :route-name 
         (fn [db]
           (-> db :kee-frame/route :data :name)))

(reg-event-fx :start-game
              (fn [{:keys [db]} [_ code]]
                {:navigate-to [:game {:code code}]}))

;; Atoms

(def join_name (r/atom ""))

(def host_name (r/atom ""))

(def current_code_text (r/atom ""))

(def game_code (r/atom ""))


;; This atom needs to be hooked up to the server
(def active_games (r/atom []))

;; Functions

(defn log [x]
  (.log js/console x))


(defn enter_game [code]
  (reset! game_code code)
  (log game_code)
  (dispatch [:start-game code]))

(def alphabet ["A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N" "O" "P" "Q" "R" "S" "T" "U" "V" "W" "X" "Y" "Z"])

(defn generate_code []
  (clojure.string/join [(rand-nth alphabet) (rand-nth alphabet) (rand-nth alphabet) (rand-nth alphabet)]))

(defn join_btn_press []
  (if (some? (some #{@current_code_text} @active_games))
    (enter_game @current_code_text)
    ())
  ;;if @current_code in @active_games, then navigate-to game/code
)

(defn host_btn_press []
  (let [code (generate_code)]
    (swap! active_games conj code)
    (enter_game code)))

(defn name_change_join [e]
  (let [text (.-value (.-target e))]
    (reset! join_name text)))

(defn name_change_host [e]
  (let [text (.-value (.-target e))]
    (reset! host_name text)))

(defn code_change [e]
  (let [text (.-value (.-target e))]
    (reset! current_code_text text)))

;; Components

(defn title_cpt []
  [:h1 "That's My Word"])

(defn join_text_cpt []
  [:h3 "Join Game"])

(defn code_input_cpt []
  [:input.gc_input
   {:type "text" :placeholder "Game Code"
    :on-change code_change}])

(defn name_input_join_cpt []
  [:input.name_input_join
   {:type "text" :placeholder "Player Name"
    :on-change name_change_join}])

(defn name_input_host_cpt []
  [:input.name_input_host
   {:type "text" :placeholder "Player Name"
    :on-change name_change_host}])

(defn join_btn_cpt []
  [:input.join_btn
   {:type "button" :value "Go!"
    :on-click join_btn_press
    :style {:width "100px"}
    :disabled (or (= (count @join_name) 0) (not= (count @current_code_text) 4))}])

(defn host_text_cpt []
  [:h3 "Host Game"])

(defn host_btn_cpt []
  [:input.host_btn
   {:type "button" :value "Go!"
    :on-click host_btn_press
    :style {:width "100px"}
    :disabled (= (count @host_name ) 0)}])



;; Containers

(defn join_container []
 [:div
  [join_text_cpt]
  [code_input_cpt]
  [:br]
  [name_input_join_cpt]
  [:br] [:br]
  [join_btn_cpt]])

(defn host_container []
 [:div
  [host_text_cpt]
  [name_input_host_cpt]
  [:br] [:br]
  [host_btn_cpt]])

(defn home_container []
  [:div
   [title_cpt]
   [join_container]
   [:br][:br][:br][:br]
   [host_container]])













;; ------------- Ingame ---------------

(defn ingame_container []
  [:div
   [:h4 (str "Code: " @game_code)]])









;; ------------- Main -----------------

(defn main []
  (let [route (subscribe [:route-name])]
    (fn []
      (case @route
        :home [home_container]
        :game [ingame_container]
        [:div "Loading..."]
        )))
)

;; -------------------------
;; Initialize app

(defn mount-root []
 (k/start! {:debug?         true
           :routes         [["/" :home]
                            ["/game/:code" :game]]
           :initial-db     {:testing true}
           :root-component [main]})
 )

(defn ^:export init! []
  (mount-root))
