import { screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';
import { server } from '@/test/server';
import { renderWithProviders } from '@/test/render';
import { taskMessages, type TaskWire } from './task';
import { TasksPage } from './TasksPage';
import {
  ALICE,
  BOB,
  aliceTask,
  allTasks,
  bobTask,
  duplicateTitleRejection,
  newTaskDraft,
  serverFailure,
  unassignedTask,
} from './tasks.fixtures';
import { fakeTaskApi, problemResponse } from './tasks.mocks';

// Accessible names the page exposes
const TITLE_FIELD = 'Title';
const ASSIGNEE_FIELD = 'Assignee';
const ADD_BUTTON = 'Add task';
const ASSIGN_BUTTON = 'Assign';
const RETRY_BUTTON = 'Retry';
const UNASSIGNED_VIEW = 'Unassigned';
const TASK_LIST = 'Tasks';

// URLs the filter writes and reads
const ALICE_URL = `?assignee=${ALICE}`;
const UNASSIGNED_URL = '?view=unassigned';

function renderPage(tasks: readonly TaskWire[] = allTasks) {
  const api = fakeTaskApi(tasks);
  server.use(...api.handlers);
  renderWithProviders(<TasksPage />);
  return api;
}

function taskItem(title: string) {
  return screen.findByRole('listitem', { name: title });
}

describe('TasksPage', () => {
  it('lists the tasks returned by the server', async () => {
    renderPage();

    const list = await screen.findByRole('list', { name: TASK_LIST });

    expect(within(list).getAllByRole('listitem')).toHaveLength(allTasks.length);
    expect(within(await taskItem(aliceTask.title)).getByText(taskMessages.assignedTo(ALICE))).toBeInTheDocument();
  });

  it('shows the empty state when there are no tasks', async () => {
    renderPage([]);

    expect(await screen.findByText(taskMessages.noTasks)).toBeInTheDocument();
  });

  it('shows an alert with a retry button when loading fails', async () => {
    server.use(problemResponse('list', serverFailure));
    renderWithProviders(<TasksPage />);

    const alert = await screen.findByRole('alert');

    expect(alert).toHaveTextContent(taskMessages.loadFailed);
    expect(within(alert).getByRole('button', { name: RETRY_BUTTON })).toBeInTheDocument();
  });

  it('adds a task and shows it in the list', async () => {
    const user = userEvent.setup();
    const api = renderPage();
    await screen.findByRole('list', { name: TASK_LIST });

    await user.type(screen.getByRole('textbox', { name: TITLE_FIELD }), newTaskDraft.title);
    await user.type(screen.getByRole('textbox', { name: ASSIGNEE_FIELD }), newTaskDraft.assignee);
    await user.click(screen.getByRole('button', { name: ADD_BUTTON }));

    expect(await taskItem(newTaskDraft.title)).toBeInTheDocument();
    expect(screen.getByRole('status')).toHaveTextContent(taskMessages.created(newTaskDraft.title));
    expect(api.createdBodies).toEqual([{ title: newTaskDraft.title, description: null, assignee: ALICE }]);
  });

  it('requires a title before sending anything to the server', async () => {
    const user = userEvent.setup();
    const api = renderPage();
    await screen.findByRole('list', { name: TASK_LIST });

    await user.click(screen.getByRole('button', { name: ADD_BUTTON }));

    expect(await screen.findByText(taskMessages.titleRequired)).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: TITLE_FIELD })).toHaveAttribute('aria-invalid', 'true');
    expect(api.createdBodies).toEqual([]);
  });

  it('shows server validation errors on the matching field and keeps the input', async () => {
    const user = userEvent.setup();
    renderPage();
    server.use(problemResponse('create', duplicateTitleRejection));
    await screen.findByRole('list', { name: TASK_LIST });

    await user.type(screen.getByRole('textbox', { name: TITLE_FIELD }), newTaskDraft.title);
    await user.click(screen.getByRole('button', { name: ADD_BUTTON }));

    const [rejection] = duplicateTitleRejection.errors;
    expect(await screen.findByText(rejection.message)).toBeInTheDocument();
    expect(screen.getByRole('textbox', { name: TITLE_FIELD })).toHaveValue(newTaskDraft.title);
  });

  it('assigns an unassigned task', async () => {
    const user = userEvent.setup();
    renderPage();
    const item = await taskItem(unassignedTask.title);

    await user.type(within(item).getByRole('textbox', { name: `Assignee for ${unassignedTask.title}` }), BOB);
    await user.click(within(item).getByRole('button', { name: ASSIGN_BUTTON }));

    expect(await within(item).findByText(taskMessages.assignedTo(BOB))).toBeInTheDocument();
  });

  it('asks the server for the assignee named in the URL', async () => {
    window.history.pushState(null, '', ALICE_URL);
    const api = renderPage();

    const list = await screen.findByRole('list', { name: TASK_LIST });

    expect(within(list).getAllByRole('listitem')).toHaveLength(1);
    expect(within(list).getByRole('listitem', { name: aliceTask.title })).toBeInTheDocument();
    expect(api.assigneeQueries).toContain(ALICE);
  });

  it('shows only unassigned tasks and records the view in the URL', async () => {
    const user = userEvent.setup();
    renderPage();
    await taskItem(bobTask.title);

    await user.click(screen.getByRole('button', { name: UNASSIGNED_VIEW }));

    const list = screen.getByRole('list', { name: TASK_LIST });
    expect(within(list).getAllByRole('listitem')).toHaveLength(1);
    expect(within(list).getByRole('listitem', { name: unassignedTask.title })).toBeInTheDocument();
    expect(window.location.search).toBe(UNASSIGNED_URL);
    expect(screen.getByRole('button', { name: UNASSIGNED_VIEW })).toHaveAttribute('aria-pressed', 'true');
  });
});
