Alphabot
========

Java irc bot
<table>
<thead>
<tr>
<th>#</th>
<th>Command</th>
<th>Syntax</th>
<th>Example</th>
<th>Descriptions</th>
</tr>
</thead>
	 <tbody>
	    <tr><td>1</td><td>Google</td><td>$google &lt;query&gt;</td><td>$google how to use google</td><td>Googles the specified query and returns the first result.</td></tr>
	    <tr><td>2</td><td>Join</td><td>$join &lt;#channel&gt;</td><td>$join #alphabot</td><td>Joins the specified channel. must start with #</td></tr>
	    <tr><td>3</td><td>Cycle</td><td>$cycle</td><td>$cycle OR $cycle #chan</td><td>Cycles the specified channel, if no channel name is specified it will cycle the channel the command was sent from</td></tr>
	    <tr><td>4</td><td>Raw</td><td>$raw</td><td>$raw privmsg #chan :boo!</td><td>Sends a raw line to the irc server.</td></tr>
	    <tr><td>5</td><td>Debug</td><td>$debug</td><td>$debug true</td><td>Sets the bot to debug (verbose) mode in system.out</td></tr>
	    <tr><td>6</td><td>Delay</td><td>$delay</td><td>$delay 5</td><td>Sets the minimum delay between messages to 5 miliseconds</td></tr>
	    <tr><td>7</td><td>Gsay</td><td>$gsay</td><td>$gsay &lt;username&gt; or &lt;#channel&gt;</td><td>Sends a message to the specified user or channel</td></tr>
	    <tr><td>8</td><td>Say</td><td>$say</td><td>$say test</td><td>Sends a message to the channel you're in</td></tr>
	    <tr><td>9</td><td>Part</td><td>$part</td><td>$part or part &lt;#channel&gt;</td><td>parts the specified channel, if none is specified defaults to the current channel</td></tr>
	    <tr><td>10</td><td>Nick</td><td>$nick</td><td>$nick Alphabot_1<td>changes the bot's nickname.</td></tr>
	    <tr><td>11</td><td>Chans</td><td>$chans</td><td>$chans<td>Lists the channels the bot is currently in.</td></tr>
	    <tr><td>12</td><td>Paid</td><td>$paid &lt;playername&gt;</td><td>$paid zack6849<td>Tells you if &lt;playername&gt; has a paid minecraft account</td></tr>
	    <tr><td>13</td><td>Mcstatus</td><td>$mcstatus</td><td>$mcstatus<td>Tells you the status of the minecraft internal servers (auth, login, session, etc)</td></tr>
	    <tr><td>14</td><td>Query</td><td>$query</td><td>$query &lt;server&gt OR $query &lt;server&gt &lt;port&gt<td>Queries the specified minecraft server and returns the player count and MOTD</td></tr>
	    <tr><td>15</td><td>Kill</td><td>$kill</td><td>$kill<td>Immediately terminates the bot's java process.</td></tr>
	    <tr><td>16</td><td>Op</td><td>$op</td><td>$op &lt;username&gt;<td>Gives the specified user operator status in the channel (note: this does not give them the op flags, just op.)</td></tr>
	    <tr><td>17</td><td>Deop</td><td>$deop</td><td>$deop &lt;username&gt;<td>Removes the specified user's operator status in the channel (note: this does not take the op flags, just op.)</td></tr>
	    <tr><td>18</td><td>Reload</td><td>$reload</td><td>$reload<td>Reloads the bot's configuration</td></tr>
	    <tr><td>19</td><td>Voice</td><td>$voice</td><td>$voice &lt;username&gt;<td>Gives the specified user voice (note: this does not give them the voice flags, just voice.)</td></tr>
	    <tr><td>20</td><td>Quiet</td><td>$quiet</td><td>$quiet &lt;username&gt;<td>Mutes the specified user by setting the +q flag on them</td></tr>
	    <tr><td>21</td><td>DeVoice</td><td>$devoice</td><td>$devoice &lt;username&gt;<td>Removes voice fromthe specified user (note: this does not take the voice flags, just voice.)</td></tr>
	    <tr><td>22</td><td>UnQuiet</td><td>$unquiet</td><td>$unquiet &lt;username&gt;<td>Un-Mutes the specified user by setting the -q flag on them</td></tr>
	    <tr><td>23</td><td>Ignore</td><td>$ignore</td><td>$ignore &lt;username&gt;<td>Instructs the bot to ignore all commands from &lt;username&gt;'s hostmask</td></tr>
	    <tr><td>24</td><td>Unignore</td><td>$unignore</td><td>$unignore &lt;username&gt;<td>Instructs the bot to remove &lt;username&gt;'s hostmask from the ignore list</td></tr>
	    <tr><td>25</td><td>setcmd</td><td>$setcmd</td><td>$setcmd &lt;command&gt; &lt;text&gt;</td><td>Sets the custom command to the specified text. you can run said command as you would any other ie $&ltcommand&gt;</td>
	 </tbody>
 </table>
 