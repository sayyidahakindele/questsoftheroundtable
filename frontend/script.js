const apiBaseUrl = "http://localhost:8080";

let started = false;
let clickable = false;
let playerPositions = ['playerBottom', 'playerLeft', 'playerTop', 'playerRight'];
let current = 1;
let done = false;

async function startGame() {
    console.log("startGame")
    try {
        const response = await fetch(`${apiBaseUrl}/start`);

        if (response.ok) {
            const result = await response.json();
            started = true;
            game = result.data;

            displayPlayers();
            displayMessage(result.message, "description");
            console.log(game)
            playGame()
        } else {
            console.error("Failed to start game. HTTP Status:", response.status);
        }
    } catch (error) {
        console.error("Error in startGame:", error);
    }
}

async function pickCard() {
	if (!clickable) return;

    console.log("pickCard")
    try {
    	const response = await fetch(`${apiBaseUrl}/pickcard`);
        if (response.ok) {
        	const result = await response.json();
            displayMessage("", "prompt");
            displayMessage(result.message, "description");
            clickable = false;
            await wait(2000)
            console.log(result)
            if (result.data.type == "EVENT") {
            	await gotEvent(JSON.stringify(result.data.value));
            	await wait(2000);
            	await trimAll();
				current = game.currentPlayer;
            	changePlayer();
                displayPlayers();
            } else if (result.data.type == "QUEST") {
            	await getSponsor(JSON.stringify(result.data.value));
            	await wait(2000);
            	if (game.currentQuest != null) {
            		current = game.currentQuest.sponsor.id;
            		changePlayer();
                    displayPlayers();
                    clickable = true;
                   	await buildQuest(JSON.stringify(result.data.value));
                   	await wait(2000)
            	}
				displayMessage("Quest is over", "prompt");
            }
        } else {
            console.error("Failed to pick card. HTTP Status:", response.status);
        }
    } catch (error) {
    	console.error("Error in pickCard:", error);
    }
}

async function playGame() {
	console.log("playGame");
	await wait(2000);
	displayMessage("Player 1's turn", "description");
	await wait(2000);
	displayMessage("Player 1 pick a card", "prompt")
	displayMessage("", "description");
	clickable = true;
}

async function gotEvent(card) {
	console.log("gotEvent");
	try {
    	const response = await fetch(`${apiBaseUrl}/gotevent?card=${card}`);
        if (response.ok) {
        	const result = await response.json();
        	game = result.data;
        	displayPlayers();
            displayMessage("", "prompt");
            displayMessage(result.message, "description");
        } else {
	        console.error("Failed to play event card. HTTP Status:", response.status);
        }
    } catch (error) {
    	console.error("Error in gotEvent:", error);
    }
}

async function trimAll() {
	console.log("trimAll")
	for (let i = 1; i <= 4; i++) {
        if (game.players[i-1].cards.cards.length > 12) {
        	current = i;
       		changePlayer();
       		displayPlayers();
            displayMessage("Pick a card to trim", "prompt");
            displayMessage("", "description");
            clickable = true;
            await trimHand(game.players[i-1].id);
            clickable = false;
        }
    }
}

async function trimHand(playerId) {
    while (true) {
        const selectedCardIndex = await waitForCardPick();
        if (selectedCardIndex === -1) continue

        try {
            const response = await fetch(`${apiBaseUrl}/trimhand`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ id: playerId, input: selectedCardIndex }),
            });

            if (!response.ok) {
                const errorText = await response.text();
                console.error("Failed to trim card:", errorText);
                continue; // Restart the loop if the backend rejects the request
            }

            const result = await response.json();
            game = result.data;
            displayPlayers()
            if (result.context == "complete") break;

        } catch (error) {
            console.error("Error in trimHand:", error);
        }
    }
}

function waitForCardPick() {
    return new Promise((resolve) => {
		const playerCards = document.querySelector('#playerBottom').querySelectorAll('.card');
        playerCards.forEach((card, index) => {
            card.addEventListener(
                'click',() => {
                	resolve(index);
                },
                { once: true } // Remove listener after one click
            );
        });
    });
}

function waitForCardOrEnd(stageNumber) {
    return new Promise((resolve) => {
    	if (stageNumber > 0) {
			const cards = document.querySelector('#playerBottom').querySelectorAll('.card')
			const pickedcards = document.querySelector(`#stage${stageNumber}`).querySelectorAll('.card')
			const doneButton = document.getElementById(`doneButton${stageNumber}`);

			cards.forEach((card, index) => {
				card.addEventListener(
					'click',
					() => {
						resolve({ type: 'card', index });
					},
					{ once: true }
				);
			});

			pickedcards.forEach((card, index) => {
				card.addEventListener(
					'click',
					() => {
						resolve({ type: 'picked', index });
					},
					{ once: true }
				);
			});

			if (doneButton) {
				doneButton.addEventListener(
					'click',
					() => {
						resolve({ type: "done" });
					},
					{ once: true }
				);
			}
    	}

		document.querySelectorAll('[class^="edit"]').forEach((editButton) => {
            editButton.addEventListener(
                'click',
                () => {
                    const editStage = parseInt(editButton.className.match(/edit(\d+)/)[1], 10);
                    console.log("edit:" + editStage)
                    resolve({ type: 'edit', stage: editStage });
                },
                { once: true }
            );
        });
    });
}

function waitForComplete() {
    return new Promise((resolve) => {
        const completeButton = document.getElementById("completeButton");
        if (completeButton) {
            completeButton.addEventListener(
                "click",
                () => {
                    console.log("Complete button clicked");
                    resolve();
                },
                { once: true }
            );
        }
    });
}

async function getSponsor(stages) {
	console.log("getSponsor")
	displayMessage("Do you want to sponsor this quest?", "prompt");
	displayMessage("", "description")
	for (let i = 1; i <= 4; i++) {
		//switch
		const selectedAnswer = await waitForYorN();
        if (selectedAnswer === "") continue;
        try {
        	const response = await fetch(`${apiBaseUrl}/getsponsor`, {
            	method: 'POST',
            	headers: { 'Content-Type': 'application/json' },
            	body: JSON.stringify({ next: i-1, input: selectedAnswer, stages: stages }),
            });

            if (!response.ok) {
            	const errorText = await response.text();
            	console.error("Failed to get sponsor:", errorText);
            	continue;
            }

            const result = await response.json();
            game = result.data;
            console.log(result)
            if (result.context == "complete") {
            	displayMessage("", "prompt");
                displayMessage(result.message, "description")
            	break;
            };

        } catch (error) {
        	console.error("Error in getSponsor:", error);
        }
    }
}

function waitForYorN() {
    return new Promise((resolve) => {
        document.querySelectorAll('.yes, .no').forEach((button) => {
            button.addEventListener(
                'click',
                (event) => {
                    resolve(event.target.classList.contains('yes') ? 'y' : 'n');
                },
                { once: true }
            );
        });
    });
}

async function buildQuest(numStages) {
	console.log("buildQuest");
	displayMessage("Building quest", "prompt");
	displayMessage("", "description");
	await wait(2000);
	displayQuest(numStages, -1)
	clickable = true;
	let currentStage = 1; // Start with stage 1

    while (currentStage <= numStages) {
        displayQuest(numStages, currentStage);
        await buildStage(currentStage);
        currentStage++;
    }
	displayQuest(numStages, 0);
	await completeBuilding();
}

async function buildStage(stage) {
	console.log(`buildStage${stage}`);

	while (true) {
        const result = await waitForCardOrEnd(stage);
        if (result.type === "card") {
			try {
				const response = await fetch(`${apiBaseUrl}/buildstage`, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ stageNum: stage, input: result.index }),
				});

				if (!response.ok) {
					const errorText = await response.text();
					console.error("Failed to add card:", errorText);
				} else {
					const result = await response.json();
					game = result.data;
					displayMessage(result.message, "description");
					console.log(game)
					displayQuest(game.currentQuest.queststagecards.length, stage);
					displayPlayers();
				}

			} catch (error) {
				console.error("Error in BuildStage:", error);
			}
        } else if (result.type === "done") {
			try {
				const response = await fetch(`${apiBaseUrl}/endstage`, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ stageNum: stage}),
				});

				if (!response.ok) {
					const errorText = await response.text();
					console.error("Failed to end stage:", errorText);
				} else {
					const result = await response.json();
					displayMessage(result.message, "description");
					if (result.context == "valid") break
				}
			} catch (error) {
        		console.error("Error in ending BuildStage:", error);
        	}
        } else if (result.type === "edit") {
        	displayQuest(game.currentQuest.queststagecards.length, result.stage);
        	await buildStage(result.stage)
        } else if (result.type == "picked") {
			try {
				const response = await fetch(`${apiBaseUrl}/addcard`, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ stageNum: stage, input: result.index}),
				});

				if (!response.ok) {
					const errorText = await response.text();
					console.error("Failed to addcard:", errorText);
				} else {
					const result = await response.json();
                    game = result.data;
					displayQuest(game.currentQuest.queststagecards.length, stage);
					displayPlayers();
				}
			} catch (error) {
        		console.error("Error in returning card in BuildStage:", error);
        	}
        }
    }


}

async function completeBuilding() {
	console.log(`completeBuilding`);

	while (true) {
		try {
			const response = await fetch(`${apiBaseUrl}/completebuilding`);
			if (!response.ok) {
				const errorText = await response.text();
				console.error("Failed to complete building:", errorText);
			} else {
				const result = await response.json();
				if (result.context == "valid") {
					const buildingDiv = document.getElementById("building");
					let completeButton = document.createElement("button");
					completeButton.id = `completeButton`;
					completeButton.classList.add(`complete`);
					completeButton.innerText = "COMPLETE";
					displayQuest(game.currentQuest.queststagecards.length, 0);
					buildingDiv.appendChild(completeButton);

					const edit = await waitForCardOrEnd(0);
					buildStage(edit.stage)

					await waitForComplete();
					break;
				}
				displayMessage(result.message, "description");
				displayQuest(game.currentQuest.queststagecards.length, result.data);
				await buildStage(result.data)
			}

		} catch (error) {
			console.error("Error in completeStage:", error);
		}
    }


}

// display
function displayQuest(numStages, stagenum) {
	const buildingDiv = document.getElementById("building");
	buildingDiv.innerHTML = "";
	for (let i=1; i<=numStages; i++) {
		let stage = document.createElement('div');
        stage.id = `stage${i}`
        stage.classList.add('stage');

    	let stageTitle = document.createElement('h3');
        stageTitle.innerText = `Stage ${i}:`;

        let cardsContainer = document.createElement('div');
        cardsContainer.id = `cards${i}`;

 		if (game.currentQuest.queststagecards[i-1].cards && Array.isArray(game.currentQuest.queststagecards[i-1].cards)) {
            game.currentQuest.queststagecards[i-1].cards.forEach((card, index) => {
                const cardDiv = document.createElement('div');
                cardDiv.classList.add('card', `picked`);
                cardDiv.innerText = `${card.code}${card.value}`;
                cardsContainer.appendChild(cardDiv);
            });
        }

        stage.appendChild(stageTitle);
        stage.appendChild(cardsContainer);

		if (game.currentQuest.queststagecards[i - 1].cards.length > 0) {
			if (i === stagenum) {
				let doneButton = document.createElement("button");
                doneButton.id = `doneButton${i}`;
                doneButton.classList.add(`done${i}`);
                doneButton.innerText = "Done";
                stage.appendChild(doneButton);
				if (i === stagenum) {
					let errorMessage = document.createElement('p');
					errorMessage.innerText = `...`;
					stage.appendChild(errorMessage);
				}
			} else {
				let doneButton = document.createElement("button");
                doneButton.id = `editButton${i}`;
                doneButton.classList.add(`edit${i}`);
                doneButton.innerText = "Edit";
                stage.appendChild(doneButton);
			}
        } else {
        	if (i === stagenum) {
        		let errorMessage = document.createElement('p');
                errorMessage.innerText = `...`;
                stage.appendChild(errorMessage);
        	}

        }
        buildingDiv.appendChild(stage)
	}
}

function changePlayer() {
    if (current == 1) {
    	playerPositions = ['playerBottom', 'playerLeft', 'playerTop', 'playerRight'];
    } else if (current == 2) {
        playerPositions = ['playerRight', 'playerBottom', 'playerLeft', 'playerTop'];
    } else if (current == 3) {
		playerPositions = ['playerTop', 'playerRight', 'playerBottom', 'playerLeft'];
    } else if (current == 4) {
		playerPositions = ['playerLeft', 'playerTop', 'playerRight', 'playerBottom'];
    }
}

function displayPlayers() {
	for (let i = 0; i < game.players.length; i++) {
        const player = game.players[i];
        const playerDiv = document.getElementById(playerPositions[i]);
        playerDiv.innerHTML = '';

        // Add player information
        const info = document.createElement('h3');
        info.id = `player${i + 1}info`;
        info.innerText = `Player ${i + 1}: ${player.shields} shields`;
        playerDiv.appendChild(info);
        const p_cards = document.createElement('div');
        p_cards.id = `cards`;


        if (player.cards && Array.isArray(player.cards.cards)) {
            player.cards.cards.forEach((card, index) => {
                const cardDiv = document.createElement('div');
                cardDiv.classList.add('card', `c${index}`);
                if (playerPositions[i] === "playerBottom") {
                     cardDiv.innerText = `${card.code}${card.value}`;
                }
                p_cards.appendChild(cardDiv);
            });
        }
        playerDiv.appendChild(p_cards);
    }
}

function displayMessage(message, section) {
	if (section == "description") {
		document.getElementById("description").innerText = message
	} else if (section == "prompt") {
    	document.getElementById("prompt").innerText = message
    }
}

function wait(ms) {
    console.log("waiting")
    return new Promise((resolve) => setTimeout(resolve, ms));
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("startGameButton").addEventListener("click", () => {
        startGame()
    });
});