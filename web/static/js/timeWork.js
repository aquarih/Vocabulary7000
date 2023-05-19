/*sidebar*/
let btn = document.querySelector('#btn');
let sidebar = document.querySelector('.sidebar');
let dropdown = document.querySelectorAll('.dropdown');
let menu_selection = document.querySelectorAll('.menu_selection');

btn.onclick = function () {
    sidebar.classList.toggle('active');
};

for (let i = 0; i < dropdown.length; i++) {
    dropdown[i].addEventListener('click', function(e) {
        menu_selection[i].classList.toggle('drop');
    });
}

/*date*/
let date_start;
let date_end;

var today = new Date();
var dd = today.getDate();
var mm = today.getMonth() + 1;
var yyyy = today.getFullYear();

if (dd < 10) {
    dd = '0' + dd;
}

if (mm < 10) {
    mm = '0' + mm;
}

today = yyyy + '-' + mm + '-' + dd;

document.getElementById("date-start").setAttribute("max", today);
document.getElementById("date-end").setAttribute("max", today);
//not yet: min為第一筆資料

console.log(today)

function start(){
    date_start = $('#date-start').val();
    console.log(date_start)

    if (date_start != null) {
        document.getElementById("date-end").setAttribute("min", date_start);
    }
}

function end() {
    date_end = $('#date-end').val();
    console.log(date_end)

    if (date_end != null) {
        document.getElementById("date-start").setAttribute("max", date_end);
    }
}

/*add a category*/
var categoryBtn = document.getElementById("addCategoriesBtn");

function createPopup(id) {
    let popupNode = document.querySelector(id);
    let overlay = popupNode.querySelector(".overlay");
    let closeBtn = popupNode.querySelector(".close-btn");

    function openPopup() {
        popupNode.classList.add("active");
    }

    function closePopup() {
        popupNode.classList.remove("active");
    }

    overlay.addEventListener("click", closePopup);
    closeBtn.addEventListener("click", closePopup);
    return openPopup;
}

let popup = createPopup('#popupModal');
categoryBtn.addEventListener("click", popup);

/*name search*/
const icon = document.querySelector('.icon');
const search = document.querySelector('.search');
icon.onclick = function () {
    search.classList.toggle('active');
}

/*add records*/
var addDetailBtn = document.getElementById("addRecord");

let detailPopup = createPopup('#detailModal');
addDetailBtn.addEventListener("click", detailPopup);

/*Timer*/
let workTitle = document.getElementById('working');
let pomodoroTitle = document.getElementById('pomodoro');
let shortBreakTitle = document.getElementById('shortBreak');
let longBreakTitle = document.getElementById('longBreak');

const timerMinutes = document.getElementById('minutes');
const timerSeconds = document.getElementById('seconds');

let startTimer;
const startButton = document.getElementById("pomodoro-start");
const pauseButton = document.getElementById("pomodoro-pause");
const stopButton = document.getElementById("pomodoro-stop");

let pomodoroTime = 25;
let shortBreakTime = 5;
let seconds = 0;
let breakCount = 0;

window.onload = () => {
    timerSeconds.innerHTML = seconds < 10 ? "0" + seconds : seconds;
    timerMinutes.innerHTML = pomodoroTime < 10 ? "0" + pomodoroTime : pomodoroTime;
    pomodoroTitle.checked = true;
};

startButton.addEventListener('click', () => {
    if (startTimer === undefined) {
        startButton.classList.add("hidden");
        pauseButton.classList.remove("hidden");
        startTimer = requestAnimationFrame(timerFunction);
    } else {
        alert("Timer is already running.");
    }
});

pauseButton.addEventListener('click', () => {
    pauseButton.classList.add("hidden");
    startButton.classList.remove("hidden");
    cancelAnimationFrame(startTimer);
    startTimer = undefined;
});

stopButton.addEventListener('click', () => {
    seconds = 0;
    breakCount = 0;
    workMinutes = pomodoroTime;
    pauseButton.classList.add("hidden");
    startButton.classList.remove("hidden");
    timerSeconds.innerHTML = seconds < 10 ? "0" + seconds : seconds;
    timerMinutes.innerHTML = pomodoroTime < 10 ? "0" + pomodoroTime : pomodoroTime;
    cancelAnimationFrame(startTimer);
    startTimer = undefined;
});

let workMinutes = pomodoroTime;
let breakMinutes = shortBreakTime;
let startTime;
let elapsedSeconds = 0;

const timerFunction = (timestamp) => {
    if (!startTimer) {
        startTimer = timestamp;
    }

    const elapsedTime = timestamp - startTimer;

    if (elapsedTime >= 1000) {
        startTime = timestamp;
        elapsedSeconds++;

        if (elapsedSeconds % 60 === 0) {
            // 每一分鐘進行一次計時器切換
            if (seconds !== 0) {
                seconds--;
            } else if (workMinutes !== 0 && seconds === 0) {
                seconds = 59;
                workMinutes--;
            } else if (workMinutes === 0 && seconds === 0) {
                cancelAnimationFrame(startTimer);
                startTimer = undefined;

                if (breakCount % 2 === 0) {
                    workMinutes = breakMinutes;
                    breakCount++;
                    pomodoroTitle.checked = false;
                    shortBreakTitle.checked = true;
                } else {
                    workMinutes = pomodoroTime;
                    breakCount++;
                    pomodoroTitle.checked = true;
                    shortBreakTitle.checked = false;
                }
            }
        }
    }

    timerSeconds.innerHTML = seconds < 10 ? "0" + seconds : seconds;
    timerMinutes.innerHTML = workMinutes < 10 ? "0" + workMinutes : workMinutes;

    if (workMinutes >= 0) {
        startTimer = requestAnimationFrame(timerFunction);
    }
};

/*todolist*/
const inputBox = document.getElementById("input-box");
const listContainer = document.getElementById("list-container");
const todo = document.querySelector('.todo');

function listIsEmptyOrNot() {
    if ($("#list-container > li").length === 0) {
        if (document.getElementById("emptyList") === null) {
            let span = document.createElement("span");
            span.innerHTML = "無任何待辦事項";
            span.setAttribute("id", 'emptyList');
            todo.appendChild(span);
        }
    } else {
        let empty = document.getElementById("emptyList");
        if (empty !== null) {
            empty.remove();
            saveData();
        }
    }
}

function addTask() {
    if (inputBox.value === '') {
        alert ("You must write SOMETHING");
    } else {
        let li = document.createElement("li");
        li.innerHTML = inputBox.value;
        listContainer.appendChild(li);
        let span = document.createElement("span");
        span.innerHTML = "\u00d7";
        li.appendChild(span);
        saveData();
    }

    inputBox.value = '';
    listIsEmptyOrNot();
}

listContainer.addEventListener("click", function (e) {
    if(e.target.tagName === "LI") {
        e.target.classList.toggle("checked");
        saveData();
    } else if (e.target.tagName === "SPAN") {
        e.target.parentElement.remove();
        listIsEmptyOrNot();
        saveData();
    }
}, false);

function saveData() {
    localStorage.setItem("data", listContainer.innerHTML);
}

function showTask() {
    listContainer.innerHTML = localStorage.getItem("data");
}

showTask();

listIsEmptyOrNot();